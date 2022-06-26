package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.Constant
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.callService
import xyz.scootaloo.thinking.lang.set
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.State
import xyz.scootaloo.thinking.server.dav.service.DAVDeleteService
import xyz.scootaloo.thinking.server.dav.service.DetectorService
import xyz.scootaloo.thinking.server.dav.service.FileTreeService
import xyz.scootaloo.thinking.server.dav.service.impl.util.DAVCommon
import xyz.scootaloo.thinking.server.dav.service.impl.util.Helper

/**
 * @author flutterdash@qq.com
 * @since 2022/6/8 12:18
 */
object DeleteImpl : SingletonVertxService(), DAVDeleteService {
    override val context = WebDAVContext.file

    override suspend fun handle(ctx: RoutingContext) {
        val param = Resolver.solveRequest(ctx)

    }

    private suspend fun deleteCheck(param: Param): Pair<Int, String> {
        val arguments = Json.obj {
            this[Constant.SUBJECT] = param.subject
            this[Headers.ifExpr] = param.ifExpr
        }
        val result = eb.callService(InternalProtocol.checker, arguments).await()
        val responseBody = result.body()
        val state = responseBody.getInteger(Constant.STATUS)
        val subject = responseBody.getString(Constant.SUBJECT)
        return state to subject
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav:delete"
        const val checker = "$prefix:checker"
    }

    private object Headers {
        const val ifExpr = "If"
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.consumer<JsonObject>(InternalProtocol.checker) {
            DeleteChecker.handle(it)
        }
    }

    private object Resolver : DAVCommon {

        fun solveRequest(ctx: RoutingContext): Param {
            val headers = ctx.request().headers()
            val subject = ctx.pathParam("*")
            return Param(
                pathNormalize(subject),
                headers[Headers.ifExpr]
            )
        }

    }

    private object DeleteChecker : DAVCommon {
        private val detector = DetectorService()
        private val fileTree = FileTreeService()

        fun handle(request: Message<JsonObject>) {
            val form = request.body()
            val subject = form.getString(Constant.SUBJECT)
            val pass = buildPass(form.getString(Headers.ifExpr))
            val state = detector.evaluate(subject, pass)

            val (dir, _) = Helper.partPath(subject)
            if (!fileTree.hasDirectory(dir)) {
                
            }

            val realPath = if (state == State.PASS) {
                Helper.fullPath(subject)
            } else {
                ""
            }

        }
    }

    private class Param(
        val subject: String,
        val ifExpr: String?,
    )

}