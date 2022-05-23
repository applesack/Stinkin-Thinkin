package xyz.scootaloo.thinking.server.dav.service

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.Immutable
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.LockImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/17 23:31
 */
interface DAVLockService : VertxService {

    @Immutable
    fun displaySupportedLock(): JsonObject

    suspend fun handle(ctx: RoutingContext)

    companion object : VertxService.SingletonFactory<DAVLockService>(LockImpl)

}