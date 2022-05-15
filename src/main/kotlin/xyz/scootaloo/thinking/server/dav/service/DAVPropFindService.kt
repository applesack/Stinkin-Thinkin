package xyz.scootaloo.thinking.server.dav.service

import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.PropFindImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/8 11:31
 */
interface DAVPropFindService : VertxService {

    suspend fun handle(ctx: RoutingContext)

    companion object : VertxService.SingletonFactory<DAVPropFindService>(PropFindImpl)

}