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
import xyz.scootaloo.thinking.server.dav.domain.core.*
import xyz.scootaloo.thinking.server.dav.service.DAVLockService
import xyz.scootaloo.thinking.server.dav.service.DetectorService
import xyz.scootaloo.thinking.server.dav.util.JsonToXml
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.server.dav.util.XmlHelper
import xyz.scootaloo.thinking.struct.http.Depth
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
        const val lockDiscovery = "LockDiscovery"
        const val lockToken = "LockToken"
        const val lockRoot = "LockRoot"
        const val activeLock = "ActiveLock"
        const val prop = "Prop"
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val lock = "$prefix:lock"
    }

    private object Headers {
        const val depth = "Depth"
        const val timeout = "Timeout"
        const val ifExpr = "If"
        const val lockToken = "Lock-Token"
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
            result[Constant.SUBJECT] = ctx.pathParam("*") ?: return null
            solveRequestHeader(result, ctx.request())
            val xmlBody = ctx.body()?.asString() ?: return result
            val (valid, body) = awaitParallelBlocking {
                solveRequestBody(xmlBody)
            }
            if (valid) {
                result[Constant.BODY] = body
            }
            return result
        }

        private fun solveRequestHeader(receiver: JsonObject, request: HttpServerRequest) {
            val headers = request.headers()
            receiver[Headers.ifExpr] = headers[Headers.ifExpr]
            receiver[Headers.timeout] = headers[Headers.timeout]
            receiver[Headers.depth] = headers[Headers.depth]
        }

        private fun solveRequestBody(xml: String): Pair<Boolean, JsonObject> {
            val body = JsonObject()
            val document = safeParseXml(log, xml) ?: return false to INVALID_JSON
            val root = document.rootElement
            root.first(Term.lockScope).ifNotNull {
                body[Term.lockScope] = solveLockScope(it)
            }
            root.first(Term.lockType).ifNotNull {
                body[Term.lockType] = solveLockType(it)
            }
            root.first(Term.owner).ifNotNull {
                val owner = solveOwner(it) ?: return@solveRequestBody false to INVALID_JSON
                body[Term.owner] = owner
            }
            return true to body
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
                return@solveOwner it.textTrim
            }
            return null
        }
    }

    private object Lock : EventbusMessageHelper, HttpHeaderHelper {
        private val detector = DetectorService()

        suspend fun handle(request: Message<JsonObject>) {
            val param = buildParam(request.body())
            if (param.lockInfo != null) {
                val (state, lock) = detector.tryLock(
                    param.subject, param.lockInfo, param.pass, fs
                )
                when (state) {
                    State.REFUSE -> {
                        makeUnCreatedResponse(request, Status.conflict)
                    }
                    State.INCOMPATIBLE -> {
                        makeUnCreatedResponse(request, Status.locked)
                    }
                    State.UNMAPPING -> {
                        makeLockSuccessResponse(request, param.subject, lock, Status.created)
                    }
                    else -> {
                        // 写入 lockDiscovery 信息, 状态码200
                        makeLockSuccessResponse(request, param.subject, lock, Status.ok)
                    }
                }
            } else {
                val notNullPass = param.pass ?: return buildRawMessage {
                    it.state = Status.bedRequest
                }.reply(request)
                val (state, lock) = detector.refreshLock(
                    param.subject, param.timeout.amount.toLong(), notNullPass
                )
                if (state == State.PASS) {
                    makeLockSuccessResponse(
                        request, param.subject, lock, Status.ok
                    )
                } else {
                    makeUnCreatedResponse(request, Status.preconditionFailed)
                }
            }
        }

        private fun makeUnCreatedResponse(request: Message<JsonObject>, code: Int) {
            buildRawMessage {
                it.state = code
            }.reply(request)
        }

        private fun makeLockSuccessResponse(
            request: Message<JsonObject>, subject: String, lock: FileLock, code: Int,
        ) {
            val activeLock = Json.obj {
                this[Term.lockType] = JsonToXml.closedTag(Term.writeLock)
                this[Term.lockScope] = JsonToXml.closedTag(lock.scope.name)

                val depth = if (lock.infinity) "infinity" else "0"
                this[Headers.depth] = JsonToXml.textTag(depth)

                this[Term.owner] = if (lock is SharedFileLock) {
                    Json.array {
                        for (owner in lock.owners) {
                            add(Json.obj {
                                this[Term.href] = owner
                            })
                        }
                    }
                } else {
                    lock as ExclusiveFileLock
                    Json.obj {
                        this[Term.href] = lock.owner
                    }
                }

                this[Headers.timeout] = lock.timeout.display()
                this[Term.lockToken] = Json.obj {
                    this[Term.href] = lock.token
                }

                this[Term.lockRoot] = Json.obj {
                    this[Term.href] = Convert.decodeUriComponent(subject)
                }
            }

            val lockDiscovery = Json.obj {
                this[Term.lockDiscovery] = Json.obj {
                    this[Term.activeLock] = activeLock
                }
            }

            buildXmlMessage(Term.prop) {
                it.state = code
                it.body = lockDiscovery
                it.putHeader(Headers.lockToken, lock.token)
            }.reply(request)
        }

        private fun buildParam(form: JsonObject): Param {
            val subject = PathUtils.normalize(
                Convert.decodeUriComponent(form.getString(Constant.SUBJECT))
            )
            val timeout = parseTimeout(form)
            val depth = parseDepth(form)
            val ifExpr = parseIfHeader(form[Headers.ifExpr] ?: "").getNullable()
            val pass = ifExpr.transformIfNotNull { Pass(it) }
            val lockInfo = if (form.getJsonObject(Constant.BODY) != null) {
                buildLockInfo(form[Constant.BODY], timeout, depth)
            } else {
                null
            }

            return Param(subject, pass, timeout, lockInfo)
        }

        private fun buildLockInfo(form: JsonObject, timeout: Timeout, depth: Depth): LockInfo {
            val scopeInfo = form.getString(Term.lockScope)
            val scope = if (scopeInfo != null && scopeInfo like Term.shared) {
                FileLockScope.SHARD
            } else {
                FileLockScope.EXCLUSIVE
            }
            return LockInfo(
                form.getString(Term.owner) ?: Constant.UNKNOWN, scope,
                timeout, depth
            )
        }

        private fun parseTimeout(form: JsonObject): Timeout {
            val timeout = form.getString(Headers.timeout) ?: ""
            val to = parseTimeoutHeader(timeout).getOrElse(defTimeout())
            if (to.amount > max)
                return Timeout(max, false)
            else if (to.amount < min)
                return defTimeout()
            return to
        }

        private fun parseDepth(form: JsonObject): Depth {
            val depth = form.getString(Headers.depth) ?: "0"
            return parseDepthHeader(depth)
        }

        private const val min = 5
        private const val max = 60 * 5
        private fun defTimeout(): Timeout {
            return Timeout(min, true)
        }
    }

    private object Status {
        const val ok = 200
        const val created = 201
        const val bedRequest = 400
        const val conflict = 409
        const val preconditionFailed = 412
        const val locked = 423
    }

    private class Param(
        val subject: String,
        val pass: Pass?,
        val timeout: Timeout,
        val lockInfo: LockInfo?,
    )

}