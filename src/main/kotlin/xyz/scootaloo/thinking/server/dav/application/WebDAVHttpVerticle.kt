package xyz.scootaloo.thinking.server.dav.application

import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter
import xyz.scootaloo.thinking.lang.getLogger

/**
 * @author flutterdash@qq.com
 * @since 2022/5/3 16:13
 */
object WebDAVHttpVerticle : VertxServiceRegisterCenter() {
    override val contextName: String = WebDAVContext.httpServer
    override val log by lazy { getLogger("http") }

    override suspend fun start() {
        val port = 2019

        val router = Router.router(vertx)
        router.get("/").handler {
            it.end("ok")
        }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(port).await()
        log.info("server started; listen port[$port]")
    }

    private suspend fun createHttpServer(port: Int): HttpServer? = try {
        vertx.createHttpServer()
    } catch (e: Throwable) {
        null
    }

    private fun configHttpRouter() {

    }

}