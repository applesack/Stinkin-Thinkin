package xyz.scootaloo.thinking.server.dav

import io.vertx.core.VertxOptions
import io.vertx.kotlin.core.vertxOptionsOf
import xyz.scootaloo.thinking.lang.Factory
import xyz.scootaloo.thinking.lang.VertxServer
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter
import xyz.scootaloo.thinking.server.component.CacheService
import xyz.scootaloo.thinking.server.component.CrontabService
import xyz.scootaloo.thinking.server.dav.application.WebDAVFileVerticle
import xyz.scootaloo.thinking.server.dav.application.WebDAVHttpVerticle
import xyz.scootaloo.thinking.server.dav.application.WebDAVStateVerticle
import xyz.scootaloo.thinking.server.dav.service.AccountService
import xyz.scootaloo.thinking.server.dav.service.DAVPropFindService

/**
 * @author flutterdash@qq.com
 * @since 2022/5/4 22:57
 */
object WebDAVServer : VertxServer() {

    override fun serverVertxOption(): VertxOptions {
        return vertxOptionsOf(
            eventLoopPoolSize = 4,
            workerPoolSize = 12
        )
    }

    override fun listVerticles(): List<VertxServiceRegisterCenter> {
        return listOf(WebDAVHttpVerticle, WebDAVStateVerticle, WebDAVFileVerticle)
    }

    override fun listServices(): List<Factory<String, VertxService>> {
        return listOf(
            CrontabService.factory(),
            CacheService.factory(),
            AccountService.factory(),
            DAVPropFindService.factory()
        )
    }

}