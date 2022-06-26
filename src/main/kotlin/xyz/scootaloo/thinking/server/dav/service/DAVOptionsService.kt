package xyz.scootaloo.thinking.server.dav.service

import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.OptionsImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/6/7 13:52
 */
interface DAVOptionsService : VertxService {

    fun handle(ctx: RoutingContext)

    companion object : VertxService.SingletonFactory<DAVOptionsService>(OptionsImpl)

}