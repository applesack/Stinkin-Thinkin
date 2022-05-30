package xyz.scootaloo.thinking.server.dav.service.fs

import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.currentTimeMillis
import xyz.scootaloo.thinking.server.dav.domain.core.*
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.struct.http.Timeout
import java.util.TreeMap
import java.util.UUID

/**
 * ## 探测器
 *
 * 可以在[FileTree.root]上放置各种标记, 还可以探测某条路径是否可达(当前权限是否可以到达该目标节点)
 *
 * @author flutterdash@qq.com
 * @since 2022/5/27 11:46
 */
object Detector {

    private val cantLockMark = arrayOf(State.REFUSE, State.CONFLICT)

    /**
     * ## 尝试给一个路径加锁
     *
     * ### 什么情况下能加锁成功?
     * 1. 从根节点起, 到目标位置之间的每一个节点都没有锁存在, 则加锁成功;
     * 如果有节点有锁的, 则锁的深度必须是0, 否则加锁失败;
     * 2. 目标位置如果不存在锁, 则加锁成功;
     * 如果已经存在锁的, 假如已有的锁和新的锁兼容, 则成功, 否则不成功;
     *
     * @param path 文件路径; 必须是经过[PathUtils.normalize]处理过的路径
     * @param lockInfo 尝试给路径加的锁
     * @param fs 文件系统工具, 此操作需要此工具辅助完成
     * @return
     * 加锁操作完成情况, 返回一个二元组;
     * 第一个值是创建锁的状态, 第二个值是实际的锁;
     * 只有当第一个值为[State.PASS]时第二个值才可用;
     */
    suspend fun tryLock(path: String, lockInfo: LockInfo, fs: FileSystem): Pair<State, FileLock> {
        try {
            val fullPath = Helper.fullPath(path)
            val exists = fs.exists(fullPath).await()
            if (!exists) {
                return State.UNMAPPING to UnreachableFileLock
            }

            val props = fs.props(fullPath).await()
            val isDirectory = props.isDirectory
            val end = if (isDirectory) 0 else 1

            val (state, node) = FileTree.searchNode(path, cantLockMark) search@{ node, depth ->
                val lock = getLock(node)
                if (lock == UnreachableFileLock) {
                    return@search State.PASS
                }
                if (depth > end) {
                    if (lock.infinity) {
                        return@search State.REFUSE
                    }
                }
                State.PASS
            }

            if (state != State.PASS) {
                return state to UnreachableFileLock
            }

            val filename = if (isDirectory) "" else PathUtils.mainName(path)
            return doLockFile(node, filename, lockInfo)
        } catch (error: Throwable) {
            return State.UNMAPPING to UnreachableFileLock
        }
    }

    fun detect() {
    }

    private fun doLockFile(
        node: SentryNode, filename: String, lockInfo: LockInfo,
    ): Pair<State, FileLock> {
        // https://www.rfc-editor.org/rfc/rfc4918#section-9.10.5
        val record = node.getRecord(filename) ?: return State.PASS to simpleMakeLock(node, filename, lockInfo)
        if (record.lock == UnreachableFileLock) {
            return State.PASS to simpleMakeLock(node, filename, lockInfo)
        }

        val lock = record.lock
        return if (lock is SharedFileLock && lockInfo.scope == FileLockScope.SHARD) {
            lock.owners.add(lockInfo.owner)
            State.PASS to lock
        } else {
            State.CONFLICT to UnreachableFileLock
        }
    }

    /**
     * 假如节点[node]位置上[filename]没有任何锁标记, 则可以直接加上[lockInfo]
     *
     * @see doLockFile
     */
    private fun simpleMakeLock(node: SentryNode, filename: String, lockInfo: LockInfo): FileLock {
        val record = node.getRecord(filename)
        val lock = createNewFileLock(lockInfo)
        if (record == null) {
            val chunk = FileMarkChunk()
            chunk.lock = lock
            node.putRecord(filename, chunk)
        } else {
            record.lock = lock
        }
        return lock
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

    private fun isCompatible(exists: FileLock, newLock: LockInfo) {

    }

    private fun getLock(node: SentryNode, file: String = ""): FileLock {
        val record = node.getRecord(file) ?: return UnreachableFileLock
        return record.lock
    }

    private fun genLockToken(): String {
        return "urn:uuid:${UUID.randomUUID()}"
    }

    /**
     * ## 超时回收器
     *
     * 管理所有锁的超时信息, 当锁的有效期已到, 则对应路径上的锁被删除[removeInvalids]
     */
    private object TimeoutRecycler {
        private val records = HashMap<String, Long>()
        private val expiryTimes = TreeMap<Long, String>()

        fun removeInvalids() {

        }

        fun addRecord(path: String, lock: FileLock) {
            addByTimeout(path, lock.timeout)
        }

        private fun addByTimeout(path: String, timeout: Timeout) {
            val currentTime = currentTimeMillis()
            val realExpiryTime = currentTime + timeout.amount

        }

    }

}

private class DetectorUnitTest : TestDsl {

    @Test
    fun test() {
        "urn:uuid:${UUID.randomUUID()}".log()
    }

}