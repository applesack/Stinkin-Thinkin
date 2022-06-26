package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.Constant
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.lang.ifNotNull
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.*
import xyz.scootaloo.thinking.server.dav.service.DetectorService
import xyz.scootaloo.thinking.server.dav.service.FileTreeService
import xyz.scootaloo.thinking.server.dav.service.impl.util.Helper
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.struct.http.Depth
import java.nio.file.NotDirectoryException
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.absolutePathString

/**
 * @author flutterdash@qq.com
 * @since 2022/6/3 0:34
 */
object FileTreeImpl : SingletonVertxService(), FileTreeService {
    override val context = WebDAVContext.file

    private val log by lazy { getLogger("file-tree") }

    private val detector by lazy { DetectorService() }

    private val root = SentryNode("/")
    private val basePath: String = Paths.get("./home").normalize().absolutePathString()

    override fun basePath(): String = basePath

    override suspend fun start() {
        scanHome()
    }

    private suspend fun scanHome() {
        val (mounted, dirCount, fileCount) = initDirectoryStruct(fs)
        log.info("path [$mounted] has been mounted, dir $dirCount, file $fileCount")
    }

    private suspend fun initDirectoryStruct(fs: FileSystem): Triple<String, Int, Int> {
        val mounted = basePath
        if (fs.exists(mounted).await()) {
            val props = fs.props(mounted).await()
            if (!props.isDirectory) {
                log.error("vfs init error", NotDirectoryException(mounted))
                return Triple(mounted, 0, 0)
            }
        } else {
            fs.mkdirs(mounted).await()
        }

        val (dirCount, fileCount, dirs) = Helper.scanDirectories(basePath, fs)
        for (dir in dirs) {
            val dirPath = PathUtils.extractRelativePath(dir, basePath)
            createVirtualDirectory(dirPath)
        }
        // 根目录不在目录数的计算之内
        return Triple(mounted, (dirCount - 1), fileCount)
    }

    /**
     * ## 简单地猜测一个文件是否存在于文件系统
     *
     * 判断逻辑: 由于本系统中维护了所有的文件夹结构, 所有本系统可以检查此文件的父文件夹是否存在于本系统;
     * 如果本系统记录了其父文件夹, 那么它有可能存在, 否则一定不存在(文件必须挂载在文件夹下, 如果文件夹不存在则文件一定不存在)
     */
    override fun guessFileExists(path: String): Boolean {
        val pathItems = Helper.pathSplit(path, '/')
        var current: SentryNode = root
        for (idx in pathItems.indices) {
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

    override suspend fun viewFiles(
        path: String, depth: Depth, pass: Pass?,
    ): List<Pair<State, AFile>> {
        if (!guessFileExists(path)) {
            return emptyList()
        }

        return FileViewer.viewFiles(path, depth, pass)
    }

    override fun hasDirectory(path: String): Boolean {
        val pathItems = Helper.pathSplit(path)
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

    override suspend fun createDirectory(path: String, force: Boolean): Int {
        try {
            val parentPath = Helper.parentPath(path)
            if (!hasDirectory(parentPath)) {
                if (force) {
                    val parentFullPath = Helper.fullPath(parentPath)
                    fs.mkdirs(parentFullPath).await()
                    createVirtualDirectory(parentPath)
                } else {
                    return 2
                }
            }

            val parentFullPath = Helper.fullPath(parentPath)
            if (!fs.exists(parentFullPath).await()) {
                return 1
            }

            fs.mkdir(Helper.fullPath(path)).await()
            createVirtualDirectory(path)
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

    override suspend fun createFile(path: String) {
        val fullPath = Helper.fullPath(path)
        if (!fs.exists(fullPath).await()) {
            fs.createFile(fullPath).await()
        }
    }

    override fun searchNode(
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

    /**
     * ## 搜索文件夹的节点
     *
     * @return
     * 如果[dirPath]是一个目录, 则最终返回对应的目录节点, 否则返回其父目录的节点
     */
    override fun searchDirNode(dirPath: String): SentryNode {
        val pathItems = Helper.pathSplit(dirPath)
        var current = root
        for (idx in pathItems.indices) {
            val item = pathItems[idx]
            if (current.hasChild(item)) {
                current = current.getChild()
            } else {
                break
            }
        }
        return current
    }

    override fun createVirtualDirectory(path: String) {
        val pathItems = Helper.pathSplit(path)
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

    private object FileViewer {

        suspend fun viewFiles(
            subject: String, depth: Depth, pass: Pass?,
        ): List<Pair<State, AFile>> {
            val fullPath = Helper.fullPath(subject)
            if (!fs.exists(fullPath).await()) {
                return listOf(State.UNMAPPING to AccessRestrictedFile(subject))
            }

            val state = detector.evaluate(subject, pass)
            if (state != State.PASS) {
                return listOf(accessRestricted(state, subject))
            }

            // [subject]位置已经确认过有访问权限

            val files = when (depth.depth) {
                0 -> {
                    this.loadSingleFile(subject)
                }
                1 -> {
                    var rsl = this.loadSingleFile(subject)
                    if (rsl.isNotEmpty()) {
                        val (rtState, _) = rsl.first
                        if (rtState == State.PASS) {
                            val tmp = loadLevelFiles(subject, pass)
                            tmp.addFirst(rsl.first)
                            rsl = tmp
                        }
                    }
                    rsl
                }
                else -> {
                    this.loadAllFiles(subject, pass)
                }
            }

            if (depth.noRoot && files.isNotEmpty()) {
                files.removeFirst()
            }

            return files
        }

        private suspend fun loadSingleFile(
            subject: String,
        ): LinkedList<Pair<State, AFile>> {
            val result = LinkedList<Pair<State, AFile>>()
            val file = NoCache.loadFile(subject) ?: return result
            result.add(State.PASS to file)
            return result
        }

        private suspend fun loadLevelFiles(
            subject: String, pass: Pass?,
        ): LinkedList<Pair<State, AFile>> {
            val monitor = detector.monitor(subject)
            val result = LinkedList<Pair<State, AFile>>()
            val files = NoCache.readDir(subject) ?: return result
            for (file in files) {
                val (_, name) = Helper.partPath(file)
                val state = monitor.evaluate(name, pass)
                if (state == State.PASS) {
                    val record = NoCache.loadFile(Helper.relative(file))
                    if (record != null) {
                        result.add(State.PASS to record)
                    } else {
                        result.add(accessRestricted(State.UNMAPPING, file))
                    }
                } else {
                    result.add(accessRestricted(state, file))
                }
            }
            return result
        }

        private suspend fun loadAllFiles(
            subject: String, pass: Pass?,
        ): LinkedList<Pair<State, AFile>> {
            val deque = LinkedList<String>() // fullPath
            val result = LinkedList<Pair<State, AFile>>()
            val monitor = detector.monitor(subject)

            deque.add(Helper.fullPath(subject))
            while (deque.isNotEmpty()) {
                val fullPath = deque.removeFirst()
                val relative = Helper.relative(fullPath)
                val (dir, name) = Helper.partPath(relative)
                if (dir != monitor.display()) {
                    monitor.update(dir)
                }

                val state = monitor.evaluate(name, pass)
                if (state == State.PASS) {
                    NoCache.loadFile(relative).ifNotNull {
                        result.add(State.PASS to it)
                        if (name.isEmpty()) {
                            NoCache.readDir(relative).ifNotNull { files ->
                                deque.addAll(files)
                            }
                        }
                    }
                } else {
                    result.add(accessRestricted(state, fullPath))
                }
            }

            return result
        }

        private fun accessRestricted(state: State, fullPath: String): Pair<State, AFile> {
            return state to AccessRestrictedFile(Helper.relative(fullPath))
        }

    }

    private object NoCache {

        suspend fun loadFile(subject: String): AFile? {
            return try {
                val fullPath = Helper.fullPath(subject)
                val props = fs.props(fullPath).await()
                File.build(fullPath, basePath, Constant.UNKNOWN, props)
            } catch (error: Throwable) {
                null
            }
        }

        suspend fun readDir(subject: String): List<String>? {
            return try {
                val fullPath = Helper.fullPath(subject)
                fs.readDir(fullPath).await()
            } catch (error: Throwable) {
                null
            }
        }

    }

}