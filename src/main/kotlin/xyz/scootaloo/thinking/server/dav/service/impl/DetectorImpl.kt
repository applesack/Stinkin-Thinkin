package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.*
import xyz.scootaloo.thinking.server.dav.service.DetectorService
import xyz.scootaloo.thinking.server.dav.service.FileTreeService
import xyz.scootaloo.thinking.server.dav.service.impl.util.Helper
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.struct.http.TokenCondition
import xyz.scootaloo.thinking.util.AbstractTimeoutRecycler
import java.util.*

/**
 * ## 探测器
 *
 * 可以在[FileTreeService]上放置各种标记, 还可以探测某条路径是否可达(当前权限是否可以到达该目标节点)
 *
 * @author flutterdash@qq.com
 * @since 2022/5/27 11:46
 */
object DetectorImpl : SingletonVertxService(), DetectorService {

    private val log by lazy { getLogger("detector") }

    override val context = WebDAVContext.file

    private val fileTree = FileTreeService()

    private val cantLockMark = arrayOf(State.REFUSE)

    override fun crontab(): VertxCrontab {
        return LockCrontab
    }

    @kotlin.jvm.Throws(Throwable::class)
    override suspend fun tryLock(
        subject: String, lockInfo: LockInfo, pass: Pass?, fs: FileSystem,
    ): Pair<State, FileLock> {
        val fullPath = Helper.fullPath(subject)
        val exists = fs.exists(fullPath).await()
        if (!exists) {
            // 目标实体不存在, 将创建资源, 并生成锁
            return State.UNMAPPING to createResourceAndLock(subject, lockInfo)
        }

        val (state, node) = searchNodeWithPass(subject, pass)

        if (state != State.PASS && state != State.HIT) {
            return state to UnreachableFileLock
        }

        val filename = if (fileTree.hasDirectory(subject)) "" else PathUtils.displayName(subject)
        return doLockFile(node, filename, lockInfo)
    }

    override fun unlock(subject: String, token: String): State {
        fun unlock(node: SentryNode, filename: String, token: String): State {
            val lock = getLock(node, filename)
            if (lock == UnreachableFileLock)
                return State.UNMAPPING
            return if (lock.token == token) {
                LockTimeoutRecycler.delRecord(Helper.buildPath(node, filename))
                State.PASS
            } else {
                State.REFUSE
            }
        }

        val (dir, file) = Helper.pathSplit(subject)
        if (!fileTree.hasDirectory(dir)) {
            return State.UNMAPPING
        }

        var dirNode = fileTree.searchDirNode(dir)
        var state = unlock(dirNode, file, token)
        if (state == State.REFUSE) {
            return State.REFUSE
        }

        while (!dirNode.isRoot()) {
            dirNode = dirNode.parent()
            state = unlock(dirNode, "", token)
            if (state == State.REFUSE)
                return State.REFUSE
            if (state == State.PASS)
                return State.PASS
        }

        return State.UNMAPPING
    }

    override fun refreshLock(subject: String, timeout: Long, pass: Pass?): Pair<State, FileLock> {
        val (dir, file) = Helper.pathSplit(subject)
        val dirNode = fileTree.searchDirNode(dir)
        val lock = getLock(dirNode, file)
        if (lock is UnreachableFileLock) {
            return State.EXPIRED to UnreachableFileLock
        }

        return if (evaluate(subject, lock, pass) == State.PASS) {
            val realTimeout = timeout * 1000
            LockTimeoutRecycler.refresh(subject, realTimeout)
            State.PASS to lock
        } else {
            State.EXPIRED to UnreachableFileLock
        }
    }

    override fun evaluate(subject: String, pass: Pass?): State {
        val (state, _) = searchNodeWithPass(subject, pass)
        return state
    }

    /**
     * ## 计算当前持有的令牌[pass]是否能够访问文件[subject]
     */
    private fun evaluate(subject: String, record: FileMarkChunk, pass: Pass?): State {
        val lock = record.lock
        if (lock == UnreachableFileLock) {
            return State.PASS
        }

        return evaluate(subject, lock, pass)
    }

    /**
     * ## 尝试使用[pass]通过[subject]的路径
     *
     * @return
     * 假如通过成功, 返回[State.PASS], 失败返回[State.REFUSE]
     */
    private fun searchNodeWithPass(subject: String, pass: Pass?): Pair<State, SentryNode> {
        val isDirectory = fileTree.hasDirectory(subject)
        val end = if (isDirectory) 0 else 1

        // 搜索目标节点, 并检查路径上有没有其他锁
        // 如果有其他锁判断一下是否有权限通过这些锁
        val pair = fileTree.searchNode(subject, cantLockMark) search@{ node, depth ->
            val lock = getLock(node)
            if (lock == UnreachableFileLock) {
                return@search State.PASS
            }

            // 除目标节点以外的其他节点
            if (depth > end) {
                if (lock.infinity) {
                    val result = evaluate(node.fullPath(), lock, pass)
                    if (result != State.PASS) {
                        // 权限不足
                        return@search result
                    }
                    return@search State.REFUSE
                }
            }
            State.PASS
        }

        val (_, file) = Helper.partPath(subject)
        val node = pair.second
        val record = node.getRecord(file)
        if (record != null) {
            return evaluate(subject, record.lock, pass) to node
        }
        if (pair.first == State.HIT)
            return State.PASS to pair.second

        return pair
    }

    private fun removeLock(path: String, owner: String) {
        val (dir, file) = Helper.pathSplit(path)
        val node = fileTree.searchDirNode(dir)
        val record = node.getRecord(file) ?: return
        val lock = record.lock
        if (lock == UnreachableFileLock)
            return
        if (lock is SharedFileLock) {
            if (owner !in lock.owners) {
                return
            }
        } else {
            lock as ExclusiveFileLock
            if (lock.owner != owner) {
                return
            }
        }
        record.lock = UnreachableFileLock
        node.putRecord(file, record)
    }

    override fun monitor(path: String): DetectorService.Monitor {
        return MonitorImpl(monitorNode(path))
    }

    private fun monitorNode(path: String): SentryNode {
        val parent = Helper.closestDirPath(path)
        return fileTree.searchDirNode(parent)
    }

    @kotlin.jvm.Throws(Throwable::class)
    private suspend fun createResourceAndLock(
        subject: String, lockInfo: LockInfo,
    ): FileLock {
        fileTree.createDirectory(subject, true)
        fileTree.createFile(subject)
        val node = fileTree.searchDirNode(subject)
        val (_, file) = Helper.partPath(subject)
        return simpleMakeLock(node, file, lockInfo)
    }

    private fun doLockFile(
        node: SentryNode, filename: String, lockInfo: LockInfo,
    ): Pair<State, FileLock> {
        // https://www.rfc-editor.org/rfc/rfc4918#section-9.10.5
        // 如果目标节点没有锁记录, 则可以直接创建锁
        val record = node.getRecord(filename) ?: return State.PASS to simpleMakeLock(node, filename, lockInfo)
        if (record.lock == UnreachableFileLock) {
            return State.PASS to simpleMakeLock(node, filename, lockInfo)
        }

        val lock = record.lock
        return if (lock is SharedFileLock && lockInfo.scope == FileLockScope.SHARD) {
            // 将新的锁持有人信息假如锁记录
            lock.owners.add(lockInfo.owner)
            State.PASS to lock
        } else {
            // 此位置已经存在锁, 但是是独占锁, 所有创建操作失败, 原因: 不兼容
            State.INCOMPATIBLE to UnreachableFileLock
        }
    }

    /**
     * 假如节点[node]位置上[filename]没有任何锁标记, 则可以直接加上[lockInfo]
     *
     * @see doLockFile
     */
    private fun simpleMakeLock(node: SentryNode, filename: String, lockInfo: LockInfo): FileLock {
        val record = node.getRecord(filename) ?: FileMarkChunk()
        val lock = createNewFileLock(lockInfo)
        record.lock = lock
        node.putRecord(filename, record)
        return lock.apply {
            LockTimeoutRecycler.addRecord(
                Helper.buildPath(node, filename), lockInfo.owner, lock
            )
        }
    }

    private fun createNewFileLock(lockInfo: LockInfo): FileLock {
        return if (lockInfo.scope == FileLockScope.SHARD) {
            SharedFileLock(
                lockInfo.timeout,
                genLockToken(),
                lockInfo.depth.depth == -1
            ).apply {
                owners.add(lockInfo.owner)
            }
        } else {
            ExclusiveFileLock(
                lockInfo.owner, lockInfo.timeout, genLockToken(),
                lockInfo.depth.depth == -1
            )
        }
    }

    /**
     * ## 返回[State.PASS]或者[State.REFUSE]
     */
    private fun evaluate(subject: String, lock: FileLock, pass: Pass?): State {
        if (lock == UnreachableFileLock)
            return State.PASS
        // 文件上有锁而当前没有令牌, 则无法通过
        val notNullPass = pass ?: return State.REFUSE
        val expression = notNullPass.token

        // 检查if表达式中是否有标记
        if (expression.tagged != null) {
            // 如果有标记, 则只使用这个标记来和当前锁匹配
            val tagged = PathUtils.normalize(expression.tagged!!)
            if (tagged != subject) {
                return State.REFUSE
            }
            val result = expression.list
                .filterIsInstance(TokenCondition::class.java).any {
                    !it.not && it.token == lock.token
                }
            return if (result) State.PASS else State.REFUSE
        } else {
            val result = expression.list.flatten()
                .filterIsInstance(TokenCondition::class.java)
                .any {
                    !it.not && it.token == lock.token
                }
            return if (result)
                State.PASS
            else
                State.REFUSE
        }
    }

    private fun getLock(node: SentryNode, file: String = ""): FileLock {
        val record = node.getRecord(file) ?: return UnreachableFileLock
        return record.lock
    }

    private fun genLockToken(): String {
        return "urn:uuid:${UUID.randomUUID()}"
    }

    private class MonitorImpl(private var node: SentryNode) : DetectorService.Monitor {
        private var displayPath: String = node.fullPath()

        override fun evaluate(filename: String, pass: Pass?): State {
            if (node.hasChild(filename)) {
                val sub = node.getChild()
                val record = sub.getRecord("") ?: return State.PASS
                return evaluate("", record, pass)
            }
            val record = node.getRecord(filename) ?: return State.PASS
            return evaluate(filename, record, pass)
        }

        override fun display(): String {
            return displayPath
        }

        override fun update(newPath: String) {
            this.node = monitorNode(newPath)
            this.displayPath = node.fullPath()
        }
    }

    private object LockCrontab : VertxCrontabAdapter() {
        override val id = "lock-gc"
        override var delay = 200L

        override fun run(currentTimeMillis: Long) {
            LockTimeoutRecycler.recycle()
        }
    }

    /**
     * ## 超时回收器
     *
     * 管理锁的超时
     */
    private object LockTimeoutRecycler : AbstractTimeoutRecycler<String, String>() {
        fun addRecord(path: String, owner: String, lock: FileLock) {
            log.info(
                "lock created;" +
                        " path: $path;" +
                        " scope: ${lock.scope};" +
                        " timeout: ${lock.timeout.amount}"
            )
            val timeout = (lock.timeout.amount * 1000L)
            putTimeoutKeyValuePair(path, owner, timeout)
        }

        fun delRecord(path: String) {
            deleteKey(path).ifNotNull { (lockPath, owner) ->
                removeLock(lockPath, owner)
            }
        }

        fun refresh(subject: String, newTimeout: Long) {
            log.info("lock refresh: path[$subject], timeout[$newTimeout]")
            refreshKeyTimeoutInfo(subject, newTimeout)
        }

        fun recycle() {
            doRecycle { (path, owner) ->
                log.info("lock recycle: path[$path], owner[$owner]")
                removeLock(path, owner)
            }
        }
    }

}