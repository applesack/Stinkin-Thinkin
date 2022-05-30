package xyz.scootaloo.thinking.server.dav.service.fs

import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.*
import xyz.scootaloo.thinking.server.dav.domain.core.State
import xyz.scootaloo.thinking.server.dav.service.FileService
import xyz.scootaloo.thinking.server.dav.util.PathUtils
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

    fun refresh(basePath: String, fs: FileSystem) {
        TODO()
    }

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
    ): List<Pair<State, AFile>> {
        val fullPath = Path(basePath, path).absolutePathString()
        val exists = fs.exists(fullPath).await()
        if (!exists) {
            return emptyList()
        }

        if (!Viewer.guessFileExists(path)) {
            return emptyList()
        }

        val result = LinkedList<Pair<State, AFile>>()
        if (depth == 0) {
//            result.add(FileCache.getSingle(fullPath, fs))
            TODO()
//            return
        }
        FileCache.getSingle(fullPath, fs)
//        val current = FileCache.handle(Helper.solveFile(fullPath, fs)) ?: return false to emptyList()
//        result.add(current)

//        if (depth == 1 && Viewer.hasDirectory(current.href)) {
//            val children = fs.readDir(fullPath).await()
//            for (childFullPath in children) {
//                FileCache.handle(Helper.solveFile(childFullPath, fs)).ifNotNull {
//                    result.add(it)
//                }
//            }
//        }

//        if (noRoot && result.isNotEmpty()) {
//            result.removeFirst()
//        }

//        return true to result
        TODO()
    }

    fun isDirectoryExists(path: String): Boolean {
        return Viewer.hasDirectory(path)
    }

    /**
     * @see FileService.createDirectory
     */
    suspend fun createDirectory(path: String, force: Boolean, fs: FileSystem): Int {
        try {
            val parentPath = Helper.parentPath(path)
            if (!Viewer.hasDirectory(parentPath)) {
                if (force) {
                    val parentFullPath = Helper.fullPath(parentPath)
                    fs.mkdirs(parentFullPath).await()
                } else {
                    return 2
                }
            }

            val fullPath = Helper.fullPath(parentPath)
            if (fs.exists(fullPath).await()) {
                return 1
            }

            fs.mkdir(path).await()
            Struct.createVirtualDirectory(path)
            return 0
        } catch (error: Throwable) {
            log.error("en error when create directory: $path", error)
            if (
                error is io.vertx.core.file.FileSystemException &&
                error.cause is java.nio.file.FileAlreadyExistsException
            ) {
                return 2
            }
            return 3
        }
    }

    @Stateless
    fun fullPath(file: String): String {
        return Helper.fullPath(file)
    }

    /**
     * ## 初始化虚拟文件系统
     *
     * @param fs 需要一个[FileSystem]对象来完成一些操作
     * @return 一个二元组, 第一个值是根目录[basePath]下文件夹个数, 第二个值是文件个数
     */
    suspend fun initDirectoryStruct(fs: FileSystem): Pair<Int, Int> {
        val (dirCount, fileCount, dirs) = Helper.scanDirectories(basePath, fs)
        for (dir in dirs) {
            val dirPath = PathUtils.extractRelativePath(dir, basePath)
            Struct.createVirtualDirectory(dirPath)
        }
        // 根目录不在目录数的计算之内
        return (dirCount - 1) to fileCount
    }

    private object Viewer {
        /**
         * ## 快速的检查文件系统中是否存在一个文件
         *
         * @param path 文件的相对路径
         * @return
         * 返回true, 该文件不一定存在
         * 返回false, 该文件一定不存在
         */
        fun guessFileExists(path: String): Boolean {
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
        fun createVirtualDirectory(path: String) {
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

    private object Interceptor {
        // 探针
        fun putSign(path: String, sign: FileLock): State {
            val pathItems = Helper.pathSplit(path)
            var current = root
            for (idx in pathItems.indices) {
                val item = pathItems[idx]
                if (current.hasChild(item)) {
                    current = current.getChild()
                } else {
                    if (idx == pathItems.lastIndex) {
                        val selected = pathItems.last()
                    } else {
                        return State.UNMAPPING
                    }
                }
            }
            TODO()
        }

        fun detect(path: String): StateValueHolder<State, SentryNode> {
//            return StateValueHolder
            TODO()
        }

        fun detect(node: SentryNode, subject: String, pass: Pass?): Boolean {
            val mark = node.getRecord(subject) ?: return true
            val lock = mark.lock
            if (lock == UnreachableFileLock) {
                return true
            } else {
                if (pass == null) {
                    return false
                }
                return pass.token == lock.token
            }
        }

        private fun extractLock(node: SentryNode, subject: String): FileLock {
            return node.getRecord(subject)?.lock ?: return UnreachableFileLock
        }

        private fun putSign(node: SentryNode, subject: String, sign: FileLock): State {
            TODO()
        }
    }

}