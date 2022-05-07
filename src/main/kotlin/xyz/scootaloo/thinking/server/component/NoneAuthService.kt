package xyz.scootaloo.thinking.server.component

import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.Authenticator
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.getLogger

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 17:10
 */
object NoneAuthService : SingletonVertxService(), Authenticator {
    override val log by lazy { getLogger("auth:none") }
    override suspend fun handle(ctx: RoutingContext) {
        ctx.next()
    }
}