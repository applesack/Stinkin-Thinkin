package xyz.scootaloo.thinking.server.dav.application

import io.vertx.core.http.HttpServer
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.VertxHttpAppAssemblyFactory
import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.dav.service.AccountService

/**
 * @author flutterdash@qq.com
 * @since 2022/5/3 16:13
 */
object WebDAVHttpVerticle : VertxServiceRegisterCenter() {
    override val contextName: String = WebDAVContext.httpServer
    override val log by lazy { getLogger("http") }

    private val accountService = AccountService()

    override suspend fun start() {
        val port = 2019

        try {
            val router = VertxHttpAppAssemblyFactory(this, accountService)
                .assembles(listOf(WebDAVHttpApplication))

            vertx.createHttpServer()
                .requestHandler(router)
                .listen(port).await()
            log.info("server started; listen port[$port]")
        } catch (error: Throwable) {
            log.error("http boot failure", error)
            closeServer()
        }
    }

}