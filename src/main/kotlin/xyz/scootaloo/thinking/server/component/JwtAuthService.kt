package xyz.scootaloo.thinking.server.component

import io.vertx.ext.web.RoutingContext

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 13:02
 */
interface JwtAuthService {

    fun handle(ctx: RoutingContext)

}