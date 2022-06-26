package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.Pass
import xyz.scootaloo.thinking.server.dav.domain.core.State
import xyz.scootaloo.thinking.server.dav.service.DAVMkColService
import xyz.scootaloo.thinking.server.dav.service.DetectorService
import xyz.scootaloo.thinking.server.dav.service.FileTreeService
import xyz.scootaloo.thinking.server.dav.service.impl.util.DAVCommon
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.util.Convert

/**
 * @author flutterdash@qq.com
 * @since 2022/5/19 16:23
 */
object MkColImpl : SingletonVertxService(), DAVMkColService, EventbusMessageHelper {
    private val log by lazy { getLogger("mkcol") }

    override val context = WebDAVContext.file

    override suspend fun handle(ctx: RoutingContext) {
        val param = Resolver.resolveRequest(ctx)
        val result = eb.callService(InternalProtocol.mkcol, param).await()
        ctx.smartReply(result.body())
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.mkcol) {
            MkCol.handle(it)
        }

        log.info("eventbus 'MkCol' service ready; current context: $contextName")
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val mkcol = "$prefix:mkcol"
    }

    private object Header {
        const val host = "Host"
        const val ifExpr = "If"
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
            val ifExpr = ctx.request().getHeader(Header.host)
            return Json.obj {
                this[Constant.SUBJECT] = path
                this[Constant.HOST] = host
                this[Header.ifExpr] = ifExpr
            }
        }
    }

    private object MkCol : EventbusMessageHelper, DAVCommon {
        private val fileTree = FileTreeService()
        private val detector = DetectorService()

        suspend fun handle(request: Message<JsonObject>) {
            val param = prepareParam(request.body())
            if (detector.evaluate(param.subject, param.pass) != State.PASS) {
                return buildRawMessage {
                    it.state = Status.forbidden
                }.reply(request)
            }

            val result = fileTree.createDirectory(param.subject)

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
                PathUtils.normalize(Convert.decodeUriComponent(path)),
                buildPass(json.getString(Header.ifExpr))
            )
        }
    }

    // https://www.rfc-editor.org/rfc/rfc4918#section-9.3.1
    private object Status {
        const val created = 201
        const val forbidden = 403
        const val notAllowed = 405
        const val conflict = 409
        const val internalError = 500
    }

    private class Param(
        val subject: String,
        val pass: Pass?,
    )
}