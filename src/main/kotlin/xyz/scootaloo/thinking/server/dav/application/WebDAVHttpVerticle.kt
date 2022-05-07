package xyz.scootaloo.thinking.server.dav.application

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
    private val log by lazy { getLogger("http") }

    override suspend fun start() {
        val router = Router.router(vertx)
        router.get("/").handler {
            it.end("ok")
        }

        log.info("hello")
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080).await()
        log.info("服务启动成功")
    }

    private fun configHttpRouter() {

    }
}