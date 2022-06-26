package xyz.scootaloo.thinking.server.component.internal

import xyz.scootaloo.thinking.lang.Context
import xyz.scootaloo.thinking.lang.VertxUtils
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.component.CacheService
import xyz.scootaloo.thinking.util.AbstractTimeoutRecycler

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 20:18
 */
@Context("state")
object CacheManager : AbstractTimeoutRecycler<String, Any>(), VertxUtils {
    private val log = getLogger("cache")

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        return valueTable[key]?.value as T
    }

    fun put(key: String, value: Any, expiryTime: Long) {
        if (expiryTime <= CacheService.invalidTime) {
            log.warn("put permanent key `$key`")
            putKeyValuePair(key, value)
            return
        }

        putTimeoutKeyValuePair(key, value, expiryTime)
    }

    fun delete(key: String) {
        deleteKey(key)
    }

    fun updateKeyExpiry(key: String, newExpiryTime: Long) {
        refreshKeyTimeoutInfo(key, newExpiryTime)
    }

    fun recycle(currentTimeMillis: Long) {
        doRecycle(currentTimeMillis) { }
    }

}