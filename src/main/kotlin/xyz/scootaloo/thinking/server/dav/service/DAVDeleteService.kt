package xyz.scootaloo.thinking.server.dav.service

import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.DeleteImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/6/8 12:18
 */
interface DAVDeleteService : VertxService {

    suspend fun handle(ctx: RoutingContext)

    companion object : VertxService.SingletonFactory<DAVDeleteService>(DeleteImpl)

}