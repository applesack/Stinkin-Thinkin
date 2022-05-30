package xyz.scootaloo.thinking.server.dav.domain.core

import xyz.scootaloo.thinking.lang.currentTimeMillis
import xyz.scootaloo.thinking.struct.http.Depth
import xyz.scootaloo.thinking.struct.http.Timeout

/**
 * @author flutterdash@qq.com
 * @since 2022/5/25 10:37
 */

abstract class FileLock(
    val scope: FileLockScope,
    val token: String,
    val infinity: Boolean,
    val timeout: Timeout,
    val creationTime: Long = currentTimeMillis(),
)

enum class FileLockScope {
    SHARD, EXCLUSIVE
}

class ExclusiveFileLock(
    val owner: String,
    timeout: Timeout,
    token: String,
    infinity: Boolean,
) : FileLock(FileLockScope.EXCLUSIVE, token, infinity, timeout)

class SharedFileLock(
    timeout: Timeout,
    token: String,
    infinity: Boolean,
    val owners: MutableList<String> = ArrayList(),
) : FileLock(FileLockScope.SHARD, token, infinity, timeout)

object UnreachableFileLock : FileLock(
    FileLockScope.SHARD, "", true, Timeout(-1, true)
)

class LockInfo(
    val owner: String,
    val scope: FileLockScope,
    val timeout: Timeout,
    val depth: Depth,
)