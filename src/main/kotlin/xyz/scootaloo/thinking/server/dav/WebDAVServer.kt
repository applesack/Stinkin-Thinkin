package xyz.scootaloo.thinking.server.dav

import io.vertx.core.VertxOptions
import io.vertx.kotlin.core.vertxOptionsOf
import xyz.scootaloo.thinking.lang.Factory
import xyz.scootaloo.thinking.lang.VertxServer
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter
import xyz.scootaloo.thinking.server.component.CacheService
import xyz.scootaloo.thinking.server.component.CrontabService

/**
 * @author flutterdash@qq.com
 * @since 2022/5/4 22:57
 */
object WebDAVServer : VertxServer() {

    override fun serverVertxOption(): VertxOptions {
        return vertxOptionsOf(
            eventLoopPoolSize = 4,
            workerPoolSize = 15
        )
    }

    override fun listVerticles(): List<VertxServiceRegisterCenter> {
        TODO("Not yet implemented")
    }

    override fun listServices(): List<Factory<VertxService>> {
        return listOf(
            CrontabService.factory(),
            CacheService.factory()
        )
    }

}