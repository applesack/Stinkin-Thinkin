package xyz.scootaloo.thinking.server.dav.service

import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.UnlockImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/6/4 0:56
 */
interface DAVUnlockService : VertxService {

    suspend fun handle(ctx: RoutingContext)

    companion object : VertxService.SingletonFactory<DAVUnlockService>(UnlockImpl)

}