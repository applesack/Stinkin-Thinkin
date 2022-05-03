package xyz.scootaloo.thinking.server.dav

import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.impl.WebDAVServiceImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/4/26 23:05
 */
interface WebDAVService : VertxService {

    override val vertx: Vertx

    suspend fun lock(ctx: RoutingContext)

    suspend fun unlock()

    companion object {
        operator fun invoke(vertx: Vertx): WebDAVService {
            return WebDAVServiceImpl.apply { this.vertx = vertx }
        }

        operator fun invoke(): WebDAVService {
            return WebDAVServiceImpl
        }
    }
}