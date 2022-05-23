package xyz.scootaloo.thinking.server.dav.service

import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.StaticResourcesImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/23 12:10
 */
interface StaticResourcesService : VertxService {

    suspend fun handle(ctx: RoutingContext)

    companion object : VertxService.SingletonFactory<StaticResourcesService>(StaticResourcesImpl)

}