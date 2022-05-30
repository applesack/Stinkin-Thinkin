package xyz.scootaloo.thinking.server.dav.service.fs

import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.coroutineScope
import xyz.scootaloo.thinking.lang.Constant
import xyz.scootaloo.thinking.lang.awaitParallelBlocking
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.domain.core.File
import xyz.scootaloo.thinking.server.dav.domain.core.State
import xyz.scootaloo.thinking.server.dav.domain.dao.FileDAO
import xyz.scootaloo.thinking.server.dav.domain.dao.UserDAO
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

/**
 * @author flutterdash@qq.com
 * @since 2022/5/27 12:19
 */
object Helper {
    fun pathSplit(path: String, sep: Char = '/'): List<String> {
        val buff = StringBuilder()
        val res = ArrayList<String>(4)
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

    suspend fun solveFile(fullPath: String, fs: FileSystem): Triple<State, String, AFile?> {
        val relative = PathUtils.extractRelativePath(fullPath, VirtualFileSystem.basePath)
        val (state, result) = FileCache.getSingle(relative, fs)
        return Triple(state, relative, result())
    }

    fun fullPath(relativePath: String): String {
        return Path(FileTree.basePath(), relativePath).absolutePathString()
    }

    fun parentPath(path: String): String {
        val idx = path.lastIndexOf('/')
        if (idx < 0)
            return "/"
        return path.substring(0, idx)
    }

    private suspend fun asyncFindFile(fullPath: String, path: String, fs: FileSystem): AFile? {
        return coroutineScope scope@{
            val exists = fs.exists(fullPath).await()
            if (!exists)
                return@scope null

            val props = fs.props(fullPath).await()
            val author = asyncFindAuthor(path)

            File.build(fullPath, VirtualFileSystem.basePath, author, props)
        }
    }

    private suspend fun asyncFindAuthor(path: String): String {
        val file = awaitParallelBlocking { FileDAO.findRecord(path) } ?: return Constant.UNKNOWN
        val user = awaitParallelBlocking { UserDAO.findById(file.author) } ?: return Constant.UNKNOWN
        return user.username
    }
}
