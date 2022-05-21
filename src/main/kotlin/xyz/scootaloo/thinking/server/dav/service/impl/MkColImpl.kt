package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.EventbusMessageHelper
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.callService
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.DAVMkColService

/**
 * @author flutterdash@qq.com
 * @since 2022/5/19 16:23
 */
object MkColImpl : SingletonVertxService(), DAVMkColService, EventbusMessageHelper {
    override val context = WebDAVContext.file

    override suspend fun handle(ctx: RoutingContext) {
        val param = Resolver.resolveRequest(ctx)
        val result = eb.callService(InternalProtocol.mkcol, param).await()
        ctx.smartReply(result.body())
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.mkcol) {
            TODO()
        }
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val mkcol = "$prefix:mkcol"
    }

    private object Cons

    private object Resolver {
        /**
         * ```json
         * {
         *     "path": string,
         *     "host": string
         * }
         * ```
         */
        fun resolveRequest(ctx: RoutingContext): JsonObject {
            val path = ctx.pathParam("*") ?: "/"
            val host = ctx.request().getHeader(ValueHeader.host) ?: "/"
            return Json.obj {

            }
        }
    }

    private object MkCol {

        fun handle() {

        }

    }

    // https://www.rfc-editor.org/rfc/rfc4918#section-9.3.1
    private object Status {
        const val created = 201
        const val forbidden = 403
        const val notAllowed = 405
        const val conflict = 409
    }

    private object ValueHeader {
        const val host = "Host"
    }

}