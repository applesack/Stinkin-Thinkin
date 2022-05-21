package xyz.scootaloo.thinking.server.dav.service

import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.MkColImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/19 16:23
 */
interface DAVMkColService : VertxService {

    suspend fun handle(ctx: RoutingContext)

    companion object : VertxService.SingletonFactory<DAVMkColService>(MkColImpl)

}