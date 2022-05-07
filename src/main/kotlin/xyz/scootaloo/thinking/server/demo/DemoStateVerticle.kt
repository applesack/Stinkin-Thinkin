package xyz.scootaloo.thinking.server.demo

import xyz.scootaloo.thinking.lang.VertxServer
import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 18:31
 */
object DemoStateVerticle : VertxServiceRegisterCenter() {

    override val contextName = DemoContext.state

    override suspend fun start() {
        initServices(DemoServer)
    }

}