package xyz.scootaloo.thinking.server.dav.service

import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.PutImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/6/7 17:57
 */
interface DAVPutService : VertxService {

    suspend fun handle(ctx: RoutingContext)

    companion object : VertxService.SingletonFactory<DAVPutService>(PutImpl)

}