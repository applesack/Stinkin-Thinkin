package xyz.scootaloo.thinking.server.dav.application

import io.vertx.ext.web.Router
import xyz.scootaloo.thinking.lang.AuthMode
import xyz.scootaloo.thinking.lang.CoroutineEntrance
import xyz.scootaloo.thinking.lang.VertxHttpApplication
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.dav.service.DAVPropFindService

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 15:17
 */
class WebDAVHttpApplication(entrance: CoroutineEntrance) : VertxHttpApplication(entrance) {

    override val log = getLogger("webdav")
    override val mountPoint = "/"
    override val auth = AuthMode.DIGEST

    private val propFindService = DAVPropFindService()

    override fun config(router: Router) = with(router) {
        propFind {
            propFindService.handle(it)
        }

        propPatch {

        }

        get {

        }

        post {

        }
    }

    companion object : InstanceFactory({ WebDAVHttpApplication(it) })
}