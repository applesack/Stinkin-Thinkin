package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.DAVMkColService
import xyz.scootaloo.thinking.server.dav.service.FileService
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.util.Convert

/**
 * @author flutterdash@qq.com
 * @since 2022/5/19 16:23
 */
object MkColImpl : SingletonVertxService(), DAVMkColService, EventbusMessageHelper {
    override val context = WebDAVContext.httpServer

    override suspend fun handle(ctx: RoutingContext) {
        val param = Resolver.resolveRequest(ctx)
        val result = eb.callService(InternalProtocol.mkcol, param).await()
        ctx.smartReply(result.body())
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.mkcol) {
            MkCol.handle(it)
        }
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val mkcol = "$prefix:mkcol"
    }

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
            val host = ctx.request().getHeader(Header.host) ?: "/"
            return Json.obj {
                this[Constant.SUBJECT] = path
                this[Constant.HOST] = host
            }
        }
    }

    private object MkCol : EventbusMessageHelper {

        private val fileService = FileService()

        suspend fun handle(request: Message<JsonObject>) {
            val param = prepareParam(request.body())
            val result = fileService.createDirectory(param.path).await()
            buildRawMessage {
                it.state = when (result) {
                    0 -> Status.created
                    1 -> Status.conflict
                    2 -> Status.notAllowed
                    else -> Status.internalError
                }
            }.reply(request)
        }

        private fun prepareParam(json: JsonObject): Param {
            val path = json.getString(Constant.SUBJECT)
            return Param(
                PathUtils.normalize(Convert.decodeUriComponent(path))
            )
        }

    }

    // https://www.rfc-editor.org/rfc/rfc4918#section-9.3.1
    private object Status {
        const val created = 201
        const val notAllowed = 405
        const val conflict = 409
        const val internalError = 500
    }

    private object Header {
        const val host = "Host"
    }

    private class Param(val path: String)

}