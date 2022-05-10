package xyz.scootaloo.thinking.server.dav.application

import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.dav.WebDAVServer

/**
 * @author flutterdash@qq.com
 * @since 2022/5/10 19:12
 */
object WebDAVFileVerticle : VertxServiceRegisterCenter() {
    override val log by lazy { getLogger("file") }
    override val contextName = WebDAVContext.file

    override suspend fun start() {
        initServices(WebDAVServer)
    }
}