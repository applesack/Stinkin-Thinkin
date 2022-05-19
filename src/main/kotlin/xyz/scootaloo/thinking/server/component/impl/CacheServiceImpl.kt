package xyz.scootaloo.thinking.server.component.impl

import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.component.CacheService
import xyz.scootaloo.thinking.server.component.internal.CacheManager

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 20:23
 */
object CacheServiceImpl : SingletonVertxService(), CacheService {
    val log by lazy { getLogger("cache") }

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

    override fun registerEventbusConsumer(contextName: String) {
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

        log.info("eventbus 'Cache' service ready; current context: $contextName")
    }

    override fun crontab(): VertxCrontab = AutoGCCacheCrontab

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

    private object AutoGCCacheCrontab : VertxCrontabAdapter() {
        override val id = "cache-service-auto-gc"
        override fun run(currentTimeMillis: Long) {
            CacheManager.gc(currentTimeMillis)
        }
    }

}