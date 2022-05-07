package xyz.scootaloo.thinking.server.component

import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import xyz.scootaloo.thinking.lang.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.contains
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.iterator
import kotlin.collections.set

/**
 * @author flutterdash@qq.com
 * @since 2022/5/4 20:01
 */
interface CacheService : VertxService {

    fun get(key: String): Future<String?>

    fun put(key: String, value: String, expiryTime: Long = defTime): Future<Unit>

    fun delete(key: String): Future<Unit>

    fun updateKeyExpiry(key: String, newExpiryTime: Long = defTime): Future<Unit>

    interface InternalApi {
        fun <T> get(key: String): T?

        fun put(key: String, value: Any, expiryTime: Long = defTime)

        fun delete(key: String)

        fun updateKeyExpiry(key: String, newExpiryTime: Long = defTime)
    }

    companion object : VertxService.SingletonFactory<CacheService>(Impl) {
        const val defTime = 5000L
        const val invalidTime = 0L
    }

    // ------------------------------ Implements ---------------------------------

    private object InternalProtocol {
        private const val prefix = "sys:cache"
        const val get = "$prefix:get"
        const val put = "$prefix:put"
        const val del = "$prefix:del"
        const val upd = "$prefix:updExp"
    }

    private object Generic {
        const val key = "key"
        const val value = "value"
        const val expiry = "expiry"
    }

    private object Impl : SingletonVertxService(), CacheService {
        val log by lazy { getLogger("cache-service") }

        override var context: String = ContextRegedit.state

        override fun get(key: String): Future<String?> {
            return eb.request<String?>(InternalProtocol.get, key).trans { it.body() }
        }

        override fun put(key: String, value: String, expiryTime: Long): Future<Unit> {
            val requestBody = Json.obj {
                this[Generic.key] = key
                this[Generic.value] = value
                this[Generic.expiry] = expiryTime
            }
            return eb.request<Unit>(InternalProtocol.put, requestBody).trans { }
        }

        override fun delete(key: String): Future<Unit> {
            return eb.request<Unit>(InternalProtocol.del, key).trans { }
        }

        override fun updateKeyExpiry(key: String, newExpiryTime: Long): Future<Unit> {
            val requestBody = Json.obj {
                this[Generic.key] = key
                this[Generic.expiry] = newExpiryTime
            }
            return eb.request<Unit>(InternalProtocol.upd, requestBody).trans { }
        }

        override fun registerEventbusConsumer() {
            eb.consumer<String>(InternalProtocol.get) { request ->
                val key = request.body()
                val value = CacheManager.get<String>(key)
                request.reply(value)
            }
            eb.consumer<JsonObject>(InternalProtocol.put) { request ->
                val json = request.body()
                CacheManager.put(
                    json.getString(Generic.key),
                    json.getString(Generic.value),
                    json.getLong(Generic.expiry)
                )
                request.reply(null)
            }
            eb.consumer<String>(InternalProtocol.del) { request ->
                val key = request.body()
                CacheManager.delete(key)
                request.reply(null)
            }
            eb.consumer<JsonObject>(InternalProtocol.upd) { request ->
                val json = request.body()
                CacheManager.updateKeyExpiry(
                    json.getString(Generic.key),
                    json.getLong(Generic.expiry)
                )
                request.reply(null)
            }
        }

        override fun crontab() = AutoGCCacheCrontab
    }

    private object CacheManager : InternalApi {
        private val kvMap = HashMap<String, CacheItem>()
        private val expiryMap = TreeMap<Long, String>()

        @Suppress("UNCHECKED_CAST")
        override fun <T> get(key: String): T? {
            val item = kvMap[key]
            return item?.value as T
        }

        override fun put(key: String, value: Any, expiryTime: Long) {
            if (key in kvMap) {
                delete(key)
            }

            if (expiryTime <= invalidTime) {
                Impl.log.warn("put permanent key `$key`")
                kvMap[key] = CacheItem(value, expiryTime)
                return
            }

            val realExpiryTime = placeExpiryKeyWithoutRepetition(key, (currentTimeMillis() + expiryTime))
            kvMap[key] = CacheItem(value, realExpiryTime)
        }

        override fun delete(key: String) {
            val item = kvMap[key] ?: return
            expiryMap remove item.expiryTime
            kvMap remove key
        }

        override fun updateKeyExpiry(key: String, newExpiryTime: Long) {
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
    }

    private object AutoGCCacheCrontab : VertxCrontabAdapter() {
        override val id = "cache-service-auto-gc"
        override fun run(currentTimeMillis: Long) {
            CacheManager.gc(currentTimeMillis)
        }
    }

    private class CacheItem(
        val value: Any,
        val expiryTime: Long,
    )

}