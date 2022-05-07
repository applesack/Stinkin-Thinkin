package xyz.scootaloo.thinking.server.demo

import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.VertxHttpAppAssemblyFactory
import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter
import xyz.scootaloo.thinking.lang.getLogger

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 18:09
 */
object DemoHttpVerticle : VertxServiceRegisterCenter() {
    private val log by lazy { getLogger("http") }

    override val contextName = DemoContext.httpServer

    override suspend fun start() {
        initServices(DemoServer)
        val router = VertxHttpAppAssemblyFactory(
            vertx, FakeUserProvider, entrance = this::startCoroutine
        ).assembles(listOf(
                DemoCacheApplication,
                DemoDigestApplication
            ))

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080).await()
        log.info("服务器启动成功")
    }

}