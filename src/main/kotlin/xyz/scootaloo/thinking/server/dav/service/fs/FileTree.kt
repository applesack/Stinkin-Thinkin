package xyz.scootaloo.thinking.server.dav.service.fs

import io.vertx.core.file.FileSystem
import xyz.scootaloo.thinking.lang.StateValueHolder
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.domain.core.Pass
import xyz.scootaloo.thinking.server.dav.domain.core.SentryNode
import xyz.scootaloo.thinking.server.dav.domain.core.State
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

/**
 * @author flutterdash@qq.com
 * @since 2022/5/27 12:19
 */
object FileTree {
    private val root = SentryNode("/")
    private lateinit var basePath: String
    private lateinit var fs: FileSystem

    fun refresh(base: String, fs: FileSystem) {
        this.fs = fs
        this.basePath = Paths.get(base).absolutePathString()
    }

    fun basePath(): String = basePath

    fun findFile(file: String, pass: Pass?): StateValueHolder<State, AFile> {
        Detector.detect()
        TODO()
    }

    /**
     * 默认[path]一定存在(无论该文件是常规文件还是目录), 按照[path]查找一个最相近的节点, 并返回;
     *
     * 调用此api之前需要先去文件系统查看该文件是否存在, 确保此api能正常工作;
     *
     * 每经过一个节点时会调用[handle]进行处理, 如果此回调返回了[ends]中的一个, 则终止处理, 并返回最后的访问的节点;
     *
     * [handle]是一个有两个参数的lambda表达式, 第一个参数是经过的节点,
     * 第二个参数是当前节点距离终点的距离, 假如搜索到了最后一项目, 则第二个参数将收到0
     */
    fun searchNode(
        path: String, ends: Array<State>, handle: (SentryNode, Int) -> State,
    ): Pair<State, SentryNode> {
        val endSet = State.merge(*ends)
        val pathItems = Helper.pathSplit(path)
        var depth = pathItems.size
        var result = handle(root, depth)
        if (State.contains(endSet, result)) {
            return result to root
        }

        var current = root
        for (idx in pathItems.indices) {
            val item = pathItems[idx]
            if (current.hasChild(item)) {
                current = current.getChild()
            } else {
                return State.HIT to current
            }
            result = handle(current, --depth)
            if (State.contains(endSet, result)) {
                return result to current
            }
        }

        return State.PASS to current
    }

    fun findDirectory(file: String, pass: Pass?) {

    }

    fun scan() {

    }

}