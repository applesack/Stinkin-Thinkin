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
import xyz.scootaloo.thinking.server.dav.service.internal.VirtualFileSystem
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.util.Convert

/**
 * @author flutterdash@qq.com
 * @since 2022/5/19 16:23
 */
object MkColImpl : SingletonVertxService(), DAVMkColService, EventbusMessageHelper {
    private val log by lazy { getLogger("MkCol") }
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

        suspend fun handle(request: Message<JsonObject>) {
            val param = prepareParam(request.body())
            if (VirtualFileSystem.isDirectoryExists(param.path)) {
                return buildRawMessage {
                    it.state = Status.conflict
                }.reply(request)
            }

            if (!VirtualFileSystem.isParentDirectoryExists(param.path)) {
                return buildRawMessage {
                    it.state = Status.notAllowed
                }.reply(request)
            }

            return try {
                VirtualFileSystem.createDirectory(param.path, fs)
                log.info("create directory: ${param.path}")
                buildRawMessage {
                    it.state = Status.created
                }.reply(request)
            } catch (error: Throwable) {
                log.error("en error when create directory: ${param.path}", error)
                buildRawMessage {
                    if (
                        error is io.vertx.core.file.FileSystemException &&
                        error.cause is java.nio.file.FileAlreadyExistsException
                    ) {
                        it.state = Status.conflict
                    } else {
                        it.state = Status.internalError
                    }
                }.reply(request)
            }
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

        //        const val forbidden = 403
        const val notAllowed = 405
        const val conflict = 409
        const val internalError = 500
    }

    private object Header {
        const val host = "Host"
    }

    private class Param(val path: String)

}