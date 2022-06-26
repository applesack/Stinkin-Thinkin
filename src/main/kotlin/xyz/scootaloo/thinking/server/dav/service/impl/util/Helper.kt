package xyz.scootaloo.thinking.server.dav.service.impl.util

import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.server.dav.domain.core.SentryNode
import xyz.scootaloo.thinking.server.dav.service.FileTreeService
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

/**
 * @author flutterdash@qq.com
 * @since 2022/5/27 12:19
 */
object Helper {

    private val fileTree by lazy { FileTreeService() }

    fun relative(fullPath: String): String {
        return PathUtils.extractRelativePath(fullPath, fileTree.basePath())
    }

    fun pathSplit(path: String, sep: Char = '/'): List<String> {
        val buff = StringBuilder()
        val res = LinkedList<String>()
        for (ch in path) {
            if (ch == sep || ch == '\\') {
                if (buff.isNotBlank()) {
                    res.add(buff.toString())
                    buff.clear()
                }
            } else {
                buff.append(ch)
            }
        }
        if (buff.isNotBlank())
            res.add(buff.toString())
        return res
    }

    suspend fun scanDirectories(basePath: String, fs: FileSystem): Triple<Int, Int, List<String>> {
        var dirCount = 0
        var fileCount = 0
        val deque = LinkedList<String>()
        val result = LinkedList<String>()
        deque.addLast(basePath)
        while (deque.isNotEmpty()) {
            val file = deque.removeFirst()
            val exists = fs.exists(file).await()
            if (exists) {
                val props = fs.props(file).await()
                if (props.isDirectory) {
                    dirCount++
                    result.addLast(file)
                    val children = fs.readDir(file).await()
                    for (child in children) {
                        deque.addLast(child)
                    }
                } else {
                    fileCount++
                }
            }
        }
        return Triple(dirCount, fileCount, result)
    }

    fun fullPath(relativePath: String): String {
        val basePath = fileTree.basePath()
        if (relativePath.startsWith(basePath)) {
            return relativePath
        }
        return Path(fileTree.basePath(), relativePath).absolutePathString()
    }

    fun partPath(path: String): Pair<String, String> {
        val closetDir = closestDirPath(path)
        return if (closetDir == path) {
            closetDir to ""
        } else {
            val file = path.substring(closetDir.length)
            closetDir to file
        }
    }

    fun closestDirPath(path: String): String {
        val node = fileTree.searchDirNode(path)
        return node.fullPath()
    }

    fun closetSuperiorDirPath(path: String): String {
        if (fileTree.hasDirectory(path)) {
            val dirNode = fileTree.searchDirNode(path)
            return dirNode.parent().fullPath()
        }
        return closestDirPath(path)
    }

    fun parentPath(path: String): String {
        val idx = path.lastIndexOf('/')
        if (idx < 0)
            return "/"
        return path.substring(0, idx)
    }

    fun buildPath(node: SentryNode, filename: String): String {
        return if (filename.isEmpty()) node.fullPath()
        else "${node.fullPath()}/$filename"
    }

}
