package xyz.scootaloo.thinking.server.demo

import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.AuthMode
import xyz.scootaloo.thinking.lang.CoroutineEntrance
import xyz.scootaloo.thinking.lang.VertxHttpApplication
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.component.CacheService

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 18:12
 */
class DemoCacheApplication(entrance: CoroutineEntrance) : VertxHttpApplication(entrance) {
    override val log = getLogger("cache")
    override val mountPoint = "/cache"
    override val auth = AuthMode.NONE

    private val cacheService = CacheService()

    override fun config(router: Router) = with(router) {
        get("/set/:key/:value") {
            val key = it.pathParam("key")
            val value = it.pathParam("value")
            cacheService.put(key, value).await()
            log.info("设置值, key=$key, value=$value")
            it.end("ok")
        }

        get("/get/:key") {
            val key = it.pathParam("key")
            val value = cacheService.get(key).await()
            log.info("取值, key=$key, value=$value")
            it.end(value ?: "null")
        }
    }

    companion object : InstanceFactory({ DemoCacheApplication(it) })
}