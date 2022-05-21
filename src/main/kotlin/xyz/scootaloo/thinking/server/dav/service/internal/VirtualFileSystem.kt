package xyz.scootaloo.thinking.server.dav.service.internal

import io.vertx.core.file.FileSystem
import io.vertx.ext.web.impl.LRUCache
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.coroutineScope
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.domain.core.File
import xyz.scootaloo.thinking.server.dav.domain.dao.FileDAO
import xyz.scootaloo.thinking.server.dav.domain.dao.UserDAO
import xyz.scootaloo.thinking.server.dav.service.internal.VirtualFileSystem.FileCache.getGroup
import xyz.scootaloo.thinking.server.dav.service.internal.VirtualFileSystem.FileCache.getSingle
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.server.dav.util.SentryNode
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 21:22
 */
@Context(WebDAVContext.file)
object VirtualFileSystem : VertxUtils {

    private val log by lazy { getLogger("vfs") }
    private val root = SentryNode("/")

    val basePath = Path("./home").normalize().absolutePathString()

    /**
     * 搜索范围内的文件信息
     *
     * @param path 相对路径(相对于基础路径[basePath])
     * @param noRoot 搜索结果中是否包含根节点
     * @param depth 搜索范围, 只处理0和1; 0即只包含[path], 1(如果[path]是一个目录, 则还会搜索其子内容)
     * @return 返回一个二元组, 第一个值标识[path]是否存在, 第二个值包含了所有满足[depth]范围内的文件信息;
     * 如果[path]不存在, 则第二个值永远为空
     */
    suspend fun viewFiles(
        path: String, depth: Int, noRoot: Boolean, fs: FileSystem,
    ): Pair<Boolean, List<AFile>> {
        val fullPath = Path(basePath, path).absolutePathString()
        val exists = fs.exists(fullPath).await()
        if (!exists) {
            return false to emptyList()
        }

        if (!Viewer.hasFile(path)) {
            return false to emptyList()
        }

        val result = LinkedList<AFile>()
        val current = FileCache.handle(Helper.solveFile(fullPath, fs)) ?: return false to emptyList()
        result.add(current)

        if (depth == 1 && Viewer.hasDirectory(current.href)) {
            val children = fs.readDir(fullPath).await()
            for (childFullPath in children) {
                FileCache.handle(Helper.solveFile(childFullPath, fs)).ifNotNull {
                    result.add(it)
                }
            }
        }

        if (noRoot && result.isNotEmpty()) {
            result.removeFirst()
        }

        return true to result
    }

    /**
     * 初始化虚拟文件系统
     *
     * @return 一个二元组, 第一个值是根目录[basePath]下文件夹个数, 第二个值是文件个数
     */
    suspend fun initDirectoryStruct(fs: FileSystem): Pair<Int, Int> {
        val (dirCount, fileCount, dirs) = Helper.scanDirectories(basePath, fs)
        for (dir in dirs) {
            val dirPath = PathUtils.extractRelativePath(dir, basePath)
            Struct.createDirectory(dirPath)
        }
        // 根目录不在目录数的计算之内
        return (dirCount - 1) to fileCount
    }

    private object Viewer {
        /**
         * 快速的检查文件系统中是否存在一个文件
         *
         * 返回true, 该文件不一定存在
         * 返回false, 该文件一定不存在
         */
        fun hasFile(path: String): Boolean {
            val pathItems = Helper.pathSplit(path, '/')
            var current: SentryNode = root
            for (idx in path.indices) {
                val item = pathItems[idx]
                if (current.hasChild(item)) {
                    current = current.getChild()
                } else {
                    // missing = items.size - idx
                    // missing == 1 directory or file missing
                    // other file with dir not exists
                    return pathItems.size - idx <= 1
                }
            }
            return true
        }

        fun hasDirectory(path: String): Boolean {
            val pathItems = Helper.pathSplit(path, '/')
            var current: SentryNode = root
            for (idx in pathItems.indices) {
                val item = pathItems[idx]
                if (current.hasChild(item)) {
                    current = current.getChild()
                } else {
                    return false
                }
            }
            return true
        }
    }

    private object Struct {
        fun createDirectory(path: String) {
            val pathItems = Helper.pathSplit(path, '/')
            var current: SentryNode = root
            for (idx in pathItems.indices) {
                val item = pathItems[idx]
                current = if (current.hasChild(item)) {
                    current.getChild()
                } else {
                    current.addChild(item)
                    current.getChild()
                }
            }
        }
    }

    /**
     * 文件缓存:
     *
     * 缓存最常浏览的文件的信息, 避免频繁访问文件系统;
     *
     * 获取单个文件 [getSingle]
     * 获取一组文件 [getGroup]
     *
     * -----------------------------------------
     * 缓存运行机制:
     *
     *
     */
    private object FileCache {
        private val cache = LRUCache<String, AFile>(512)

        suspend fun getSingle(path: String): AFile? {
            TODO()
        }

        suspend fun getGroup(path: String): List<AFile> {
            TODO()
        }

        fun getFile(path: String): AFile? {
            return cache[path]
        }

        // hit, relative, file
        fun handle(tripe: Triple<Boolean, String, AFile?>): AFile? {
            val file = tripe.third
            if (!tripe.first && file != null) {
                putFile(tripe.second, file)
            }
            return file
        }

        private fun putFile(path: String, file: AFile) {
            cache[path] = file
        }
    }

    private object Helper {
        fun pathSplit(path: String, sep: Char): List<String> {
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

        suspend fun solveFile(fullPath: String, fs: FileSystem): Triple<Boolean, String, AFile?> {
            val relative = PathUtils.extractRelativePath(fullPath, basePath)
            val exists = FileCache.getFile(relative)
            if (exists != null)
                return Triple(true, relative, exists)

            return Triple(false, relative, asyncFindFile(fullPath, relative, fs))
        }

        private suspend fun asyncFindFile(fullPath: String, path: String, fs: FileSystem): AFile? {
            return coroutineScope scope@{
                val exists = fs.exists(fullPath).await()
                if (!exists)
                    return@scope null

                val props = fs.props(fullPath).await()
                val author = asyncFindAuthor(path)

                File.build(fullPath, basePath, author, props)
            }
        }

        private suspend fun asyncFindAuthor(path: String): String {
            val file = awaitParallelBlocking { FileDAO.findRecord(path) } ?: return Constant.UNKNOWN
            val user = awaitParallelBlocking { UserDAO.findById(file.author) } ?: return Constant.UNKNOWN
            return user.username
        }
    }

}