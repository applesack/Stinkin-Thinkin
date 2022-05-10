package xyz.scootaloo.thinking.server.demo

import org.slf4j.Logger
import xyz.scootaloo.thinking.lang.VertxServer
import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter
import xyz.scootaloo.thinking.lang.getLogger

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 18:31
 */
object DemoStateVerticle : VertxServiceRegisterCenter() {
    override val log by lazy { getLogger("state") }

    override val contextName = DemoContext.state

    override suspend fun start() {
        initServices(DemoServer)
    }

}