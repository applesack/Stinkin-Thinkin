package xyz.scootaloo.thinking.server.demo

import io.vertx.ext.web.Router
import xyz.scootaloo.thinking.lang.AuthMode
import xyz.scootaloo.thinking.lang.CoroutineEntrance
import xyz.scootaloo.thinking.lang.VertxHttpApplication
import xyz.scootaloo.thinking.lang.getLogger

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 18:47
 */
class DemoDigestApplication(entrance: CoroutineEntrance) : VertxHttpApplication(entrance) {

    override val log = getLogger("digest")
    override val mountPoint = "/digest"
    override val auth = AuthMode.DIGEST

    override fun config(router: Router) = with(router) {
        get {
            it.end("hello")
        }
    }

    companion object : InstanceFactory({ DemoDigestApplication(it) })
}