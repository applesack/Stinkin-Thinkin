package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import org.dom4j.Element
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.lib.HttpHeaderHelper
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.FileLockScope
import xyz.scootaloo.thinking.server.dav.domain.core.LockInfo
import xyz.scootaloo.thinking.server.dav.service.DAVLockService
import xyz.scootaloo.thinking.server.dav.util.JsonToXml
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.server.dav.util.XmlHelper
import xyz.scootaloo.thinking.struct.http.Depth
import xyz.scootaloo.thinking.struct.http.IfExpression
import xyz.scootaloo.thinking.struct.http.Timeout
import xyz.scootaloo.thinking.util.Convert

/**
 * @author flutterdash@qq.com
 * @since 2022/5/17 23:32
 */
object LockImpl : SingletonVertxService(), DAVLockService, EventbusMessageHelper {
    private val log by lazy { getLogger("lock") }
    override val context = WebDAVContext.file

    override fun displaySupportedLock(): JsonObject {
        return supported
    }

    override suspend fun handle(ctx: RoutingContext) {
        val arguments = Resolver.solveRequest(ctx) ?: return ctx.fail(Status.bedRequest)
        val result = eb.callService(InternalProtocol.lock, arguments).await()
        ctx.smartReply(result.body())
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.lock) {
            Lock.handle(it)
        }

        log.info("eventbus 'Lock' service ready; current context: $contextName")
    }

    private object Term {
        const val lockEntry = "LockEntry"
        const val lockScope = "LockScope"
        const val lockType = "LockType"
        const val writeLock = "Write"
        const val shared = "Shared"
        const val exclusive = "Exclusive"
        const val owner = "Owner"
        const val href = "Href"
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val lock = "$prefix:lock"
    }

    private object Headers {
        const val depth = "Depth"
        const val timeout = "Timeout"
        const val ifExpr = "If"
    }

    private val supported = Json.obj {
        this[Term.lockEntry] = Json.array {
            add(Json.obj {
                this[Term.lockScope] = Json.obj {
                    this[Term.exclusive] = JsonToXml.closedTag()
                }
                this[Term.lockType] = Json.obj {
                    this[Term.writeLock] = JsonToXml.closedTag()
                }
            })
            add(Json.obj {
                this[Term.lockScope] = Json.obj {
                    this[Term.shared] = JsonToXml.closedTag()
                }
                this[Term.lockType] = Json.obj {
                    this[Term.writeLock] = JsonToXml.closedTag()
                }
            })
        }
    }

    private object Resolver : XmlHelper {
        /**
         * ## lockInfo
         *
         * ```json
         * {
         *     "Timeout": string?,
         *     "Depth": string?,
         *     "If": string?
         *     "Body": {
         *         "LockScope": string?,
         *         "LockType": string?,
         *         "Owner": string
         *     }?
         * }
         * ```
         *
         * 请求体中如果是xml格式的内容, 那么一定要带有作用域, 所有者信息, 否则视为无效的请求
         */
        suspend fun solveRequest(ctx: RoutingContext): JsonObject? {
            val result = JsonObject()
            solveRequestHeader(result, ctx.request()) ?: return null
            val xmlBody = ctx.body()?.asString() ?: return null
            val (state, body) = awaitParallelBlocking {
                solveRequestBody(xmlBody)
            }
            if (state < 0) {
                return null
            } else {
                result[Constant.BODY] = body
            }
            return result
        }

        private const val symbol = 1

        private fun solveRequestHeader(receiver: JsonObject, request: HttpServerRequest): Int? {
            val headers = request.headers()
            receiver[Headers.ifExpr] = headers[Headers.ifExpr]
            receiver[Headers.timeout] = headers[Headers.timeout]
            receiver[Headers.depth] = headers[Headers.depth]

            if (!(Headers.depth in receiver && receiver.getString(Headers.depth) != null))
                return null
            return symbol
        }

        private fun solveRequestBody(xml: String): Pair<Int, JsonObject?> {
            val body = JsonObject()
            val document = safeParseXml(log, xml) ?: return 0 to null
            val root = document.rootElement
            root.first(Term.lockScope).ifNotNull {
                body[Term.lockScope] = solveLockScope(it)
            }
            root.first(Term.lockType).ifNotNull {
                body[Term.lockType] = solveLockType(it)
            }
            root.first(Term.owner).ifNotNull {
                body[Term.owner] = solveOwner(it)
                return@solveRequestBody -1 to null
            }
            return 0 to body
        }

        private fun solveLockScope(lockScope: Element): String? {
            val children = lockScope.collectChildTags()
            return if (children.isEmpty()) null
            else children.first()
        }

        private fun solveLockType(lockType: Element): String? {
            val children = lockType.collectChildTags()
            return if (children.isEmpty()) null
            else return children.first()
        }

        private fun solveOwner(ownerLabel: Element): String? {
            ownerLabel.first(Term.href).ifNotNull {
                return@solveOwner it.name
            }
            return null
        }
    }

    private object Lock : EventbusMessageHelper, HttpHeaderHelper {
        fun handle(request: Message<JsonObject>) {
            val param = buildParam(request.body())
            if (param.ifExpr != null) {
                tryUnlock(request, param)
            } else {
                tryLock(request, param)
            }
        }

        private fun tryUnlock(request: Message<JsonObject>, param: Param) {
            TODO()
        }

        private fun tryLock(request: Message<JsonObject>, param: Param) {
            TODO()
        }

        private fun buildParam(form: JsonObject): Param {
            val subject = PathUtils.normalize(
                Convert.decodeUriComponent(form.getString(Constant.SUBJECT))
            )
            val depth = parseDepthHeader(form[Headers.depth])
            val timeout = parseTimeoutHeader(form[Headers.timeout] ?: "").getOfNull()
            val ifExpr = parseIfHeader(form[Headers.ifExpr] ?: "").getOfNull()
            val lockInfo = if (form.getJsonObject(Constant.BODY) != null) {
                buildParamBody(subject, form[Constant.BODY])
            } else {
                null
            }

            return Param(subject, depth, timeout, ifExpr, lockInfo)
        }

        private fun buildParamBody(subject: String, form: JsonObject): LockInfo {
            val scopeInfo = form.getString(Term.lockScope)
            val scope = if (scopeInfo != null && scopeInfo like Term.shared) {
                FileLockScope.SHARD
            } else {
                FileLockScope.EXCLUSIVE
            }
            return LockInfo(
                form.getString(Term.owner), scope,
                parseTimeout(form.getString(Headers.timeout)),
                parseDepth(form.getString(Headers.depth))
            )
        }

        private fun parseTimeout(timeout: String?): Timeout {
            return parseTimeoutHeader(timeout ?: "").getOrElse(defTimeout())
        }

        private fun parseDepth(depth: String?): Depth {
            return parseDepthHeader(depth ?: "")
        }

        private const val min = 30
        private const val max = 60 * 5
        private fun defTimeout(): Timeout {
            return Timeout(min, true)
        }

        private fun defDepth(): Depth {
            return Depth(0, false)
        }
    }

    private object Status {
        const val ok = 200
        const val created = 201
        const val bedRequest = 400
        const val conflict = 409
        const val preconditionFailed = 412
        const val unprocessableEntity = 422
        const val locked = 423
    }

    private class Param(
        val subject: String,
        val depth: Depth?,
        val timeout: Timeout?,
        val ifExpr: IfExpression?,
        val lockInfo: LockInfo?,
    )

}