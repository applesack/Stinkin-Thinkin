package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.State
import xyz.scootaloo.thinking.server.dav.service.DAVPutService
import xyz.scootaloo.thinking.server.dav.service.DetectorService
import xyz.scootaloo.thinking.server.dav.service.FileTreeService
import xyz.scootaloo.thinking.server.dav.service.impl.util.DAVCommon
import xyz.scootaloo.thinking.server.dav.service.impl.util.Helper
import xyz.scootaloo.thinking.server.dav.util.PathUtils

/**
 * @author flutterdash@qq.com
 * @since 2022/6/7 17:57
 */
object PutImpl : SingletonVertxService(), DAVPutService {
    private val log by lazy { getLogger("put") }

    override val context = WebDAVContext.file

    override suspend fun handle(ctx: RoutingContext) {
        val param = Resolver.buildParam(ctx)
        val (state, path) = uploadCheck(param)
        val response = ctx.response()
        when (state) {
            1 -> {
                response.statusCode = Status.precondition
            }
            2 -> {
                response.statusCode = Status.conflict
            }
            else -> {
                if (!doUpload(param, path)) {
                    response.statusCode = Status.internalError
                }
            }
        }
        ctx.end()
    }

    /**
     * 0 允许上传
     * 1 权限缺失
     * 2 父目录缺失
     */
    private suspend fun uploadCheck(param: Param): Pair<Int, String> {
        val argument = Json.obj {
            this[Constant.SUBJECT] = param.uploadPath
            this[Header.ifExpr] = param.ifExpr
        }
        val json = eb.callService(InternalProtocol.putChecker, argument).await().body()
        val status = json.getInteger(Constant.STATUS)
        val path = if (status == 0) json.getString(Constant.SUBJECT) else ""
        return status to path
    }

    private suspend fun doUpload(param: Param, realPath: String): Boolean {
        return try {
            var target = realPath
            if (!param.overwrite && fs.exists(target).await()) {
                while (fs.exists(target).await()) {
                    target = PathUtils.generateNoDuplicateName(target)
                }
            }

            log.info("upload file to '$target'")
            fs.writeFile(target, param.buffer).await()
            true
        } catch (error: Throwable) {
            log.error("en error when do upload file: ${param.uploadPath}", error)
            false
        }
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.consumer<JsonObject>(InternalProtocol.putChecker) {
            FileUploadChecker.handle(it)
        }

        log.info("eventbus 'Put' service ready; current context: $contextName")
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val putChecker = "$prefix:putChecker"
    }

    private object Header {
        const val ifExpr = "If"
        const val overwrite = "Overwrite"
    }

    private object Resolver : DAVCommon {
        fun buildParam(ctx: RoutingContext): Param {
            val headers = ctx.request().headers()
            val overwrite = headers[Header.overwrite]
            val subject = pathNormalize(ctx.pathParam("*"))
            val ifExpr = headers[Header.ifExpr]
            val body = ctx.body()
            return Param(
                solveOverwrite(overwrite),
                body.buffer(),
                subject, ifExpr
            )
        }

        private fun solveOverwrite(text: String?): Boolean {
            text ?: return true
            return text == "T"
        }
    }

    private object FileUploadChecker : DAVCommon {
        private val detector = DetectorService()
        private val fileTree = FileTreeService()

        @Context("file")
        fun handle(request: Message<JsonObject>) {
            val body = request.body()
            val subject = body.getString(Constant.SUBJECT)
            val pass = buildPass(body.getString(Header.ifExpr))
            val (dir, _) = Helper.partPath(subject)
            val state = detector.evaluate(dir, pass)

            val result = JsonObject()
            if (state != State.PASS) {
                result[Constant.STATUS] = 1
                return reply(request, result)
            }

            if (!fileTree.hasDirectory(dir)) {
                result[Constant.STATUS] = 2
                return reply(request, result)
            }

            result[Constant.STATUS] = 0
            result[Constant.SUBJECT] = Helper.fullPath(subject)
            reply(request, result)
        }

        private fun reply(request: Message<JsonObject>, json: JsonObject) {
            request.reply(json)
        }

    }

    private object Status {
        const val conflict = 409
        const val precondition = 412
        const val internalError = 500
    }

    private class Param(
        val overwrite: Boolean,
        val buffer: Buffer,
        val uploadPath: String,
        val ifExpr: String?,
    )

}