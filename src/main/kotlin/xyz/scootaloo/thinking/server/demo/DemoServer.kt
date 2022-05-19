package xyz.scootaloo.thinking.server.demo

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
 * @since 2022/5/6 18:07
 */
object DemoServer : VertxServer() {
    override fun serverVertxOption(): VertxOptions {
        return vertxOptionsOf(eventLoopPoolSize = 4, workerPoolSize = 4)
    }

    override fun listVerticles(): List<VertxServiceRegisterCenter> {
        return listOf(
            DemoHttpVerticle, DemoStateVerticle
        )
    }

    override fun listServices(): List<Factory<String, VertxService>> {
        return listOf(
            CrontabService.factory(),
            CacheService.factory()
        )
    }

}