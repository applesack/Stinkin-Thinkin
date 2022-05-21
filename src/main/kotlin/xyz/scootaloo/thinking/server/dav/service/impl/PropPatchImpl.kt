package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.EventbusMessageHelper
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.DAVPropPatchService
import xyz.scootaloo.thinking.server.dav.util.XmlHelper

/**
 * @author flutterdash@qq.com
 * @since 2022/5/18 22:51
 */
object PropPatchImpl : SingletonVertxService(), DAVPropPatchService, EventbusMessageHelper {

    private val log by lazy { getLogger("prop-patch") }
    override val context = WebDAVContext.file

    override suspend fun handle(ctx: RoutingContext) {
        ctx.fail(403)
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.propPatch) {
        }
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val propPatch = "$prefix:propPatch"
    }

    private object Resolver : XmlHelper {

        /**
         * ```json
         * {
         *     "subject": string,
         * }
         * ```
         */
        suspend fun resolve(ctx: RoutingContext): Pair<Boolean, JsonObject> {
            TODO()
        }

    }

    private object PropPatch
}