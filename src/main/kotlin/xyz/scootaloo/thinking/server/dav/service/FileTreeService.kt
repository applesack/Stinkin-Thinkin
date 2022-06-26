package xyz.scootaloo.thinking.server.dav.service

import io.vertx.core.Future
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.domain.core.Pass
import xyz.scootaloo.thinking.server.dav.domain.core.SentryNode
import xyz.scootaloo.thinking.server.dav.domain.core.State
import xyz.scootaloo.thinking.server.dav.service.impl.FileTreeImpl
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.struct.http.Depth

/**
 * @author flutterdash@qq.com
 * @since 2022/5/27 12:19
 */
interface FileTreeService : VertxService {

    fun basePath(): String

    fun guessFileExists(path: String): Boolean

    suspend fun viewFiles(
        path: String, depth: Depth, pass: Pass?,
    ): List<Pair<State, AFile>>

    fun hasDirectory(path: String): Boolean

    /**
     * ## 创建文件夹, 返回创建操作的执行情况
     *
     * @param path 要创建的文件夹的相对路径; 这个操作要求[path]所指向的目录不存在,
     * 同时如果[path]有父目录, 那么它的父目录必须存在, 否则创建不成功;
     * **这个路径必须经过[PathUtils.normalize]格式化**;
     *
     * @param force 如果启用这个参数, 当[path]所在的父目录不存在时, 会自动创建;
     *
     * @return 一个[Future]对象, 其中包含了创建操作的完成情况
     * - 0 创建成功
     * - 1 创建失败: 目标文件已存在
     * - 2 创建失败: 目标位置的父目录不存在
     * - 3 创建失败: 创建的过程中出现内部错误
     */
    suspend fun createDirectory(path: String, force: Boolean = false): Int

    suspend fun createFile(path: String)

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
    ): Pair<State, SentryNode>

    fun searchDirNode(dirPath: String): SentryNode

    fun createVirtualDirectory(path: String)

    companion object : VertxService.SingletonFactory<FileTreeService>(FileTreeImpl)

}