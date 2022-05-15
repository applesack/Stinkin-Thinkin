package xyz.scootaloo.thinking.server.component.internal

import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.component.CacheService
import java.util.*
import kotlin.collections.HashMap

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 20:18
 */
object CacheManager : VertxUtils {
    private val log = getLogger("cache")
    private val kvMap = HashMap<String, CacheItem>()
    private val expiryMap = TreeMap<Long, String>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val item = kvMap[key]
        return item?.value as T
    }

    fun put(key: String, value: Any, expiryTime: Long) {
        if (key in kvMap) {
            delete(key)
        }

        if (expiryTime <= CacheService.invalidTime) {
            log.warn("put permanent key `$key`")
            kvMap[key] = CacheItem(value, expiryTime)
            return
        }

        val realExpiryTime = placeExpiryKeyWithoutRepetition(key, (currentTimeMillis() + expiryTime))
        kvMap[key] = CacheItem(value, realExpiryTime)
    }

    fun delete(key: String) {
        val item = kvMap[key] ?: return
        expiryMap remove item.expiryTime
        kvMap remove key
    }

    fun updateKeyExpiry(key: String, newExpiryTime: Long) {
        val item = kvMap[key] ?: return
        delete(key)
        put(key, item.value, newExpiryTime)
    }

    fun gc(currentTimeMillis: Long) {
        if (kvMap.isEmpty() || expiryMap.isEmpty())
            return

        val invalidKeys = LinkedList<String>()
        for ((expiry, key) in expiryMap) {
            if (expiry > currentTimeMillis)
                break
            invalidKeys add key
        }

        if (invalidKeys.isNotEmpty()) {
            invalidKeys.forEach(::delete)
        }
    }

    private fun placeExpiryKeyWithoutRepetition(key: String, expiryTime: Long): Long {
        if (expiryTime !in expiryMap) {
            expiryMap[expiryTime] = key
            return expiryTime
        }

        var validExpiryTimePst = expiryTime - 1
        var validExpiryTimeFut = expiryTime + 1

        while (true) {
            if (validExpiryTimePst !in expiryMap) {
                expiryMap[validExpiryTimePst] = key
                return validExpiryTimePst
            } else {
                validExpiryTimePst--
            }

            if (validExpiryTimeFut !in expiryMap) {
                expiryMap[validExpiryTimeFut] = key
                return validExpiryTimeFut
            } else {
                validExpiryTimeFut++
            }
        }
    }

    private class CacheItem(
        val value: Any,
        val expiryTime: Long,
    )
}