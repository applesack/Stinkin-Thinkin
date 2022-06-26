package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.State
import xyz.scootaloo.thinking.server.dav.service.DAVUnlockService
import xyz.scootaloo.thinking.server.dav.service.DetectorService
import xyz.scootaloo.thinking.util.Convert

/**
 * @author flutterdash@qq.com
 * @since 2022/6/4 0:56
 */
object UnlockImpl : SingletonVertxService(), DAVUnlockService, EventbusMessageHelper {
    override val context = WebDAVContext.file

    override suspend fun handle(ctx: RoutingContext) {
        val arguments = Resolver.resolveRequest(ctx) ?: return ctx.fail(Status.badRequest)
        val result = eb.callService(InternalProtocol.unlock, arguments).await()
        ctx.smartReply(result.body())
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.unlock) {
            Unlock.handle(it)
        }
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val unlock = "$prefix:unlock"
    }

    private object Term {
        const val lockToken = "Lock-Token"
    }

    private object Resolver {
        fun resolveRequest(ctx: RoutingContext): JsonObject? {
            val headers = ctx.request().headers()
            val subject = ctx.pathParam("*") ?: "/"
            val lockToken = headers[Term.lockToken] ?: return null
            return Json.obj {
                this[Constant.SUBJECT] = subject
                this[Term.lockToken] = lockToken
            }
        }
    }

    private object Unlock : EventbusMessageHelper {

        private val detector = DetectorService()

        fun handle(request: Message<JsonObject>) {
            val param = buildParam(request.body())
            when (detector.unlock(param.subject, param.lockToken)) {
                State.UNMAPPING -> {
                    buildRawMessage { it.state = Status.conflict }.reply(request)
                }
                State.REFUSE -> {
                    buildRawMessage { it.state = Status.forbidden }.reply(request)
                }
                else -> {
                    buildRawMessage { it.state = Status.noContent }.reply(request)
                }
            }
        }

        private fun buildParam(form: JsonObject): Param {
            return Param(
                subject = Convert.decodeUriComponent(form.getString(Constant.SUBJECT)),
                lockToken = form.getString(Term.lockToken)!!
            )
        }

    }

    private object Status {
        const val noContent = 204
        const val badRequest = 400
        const val forbidden = 403
        const val conflict = 409
    }

    private class Param(
        val subject: String,
        val lockToken: String,
    )

}