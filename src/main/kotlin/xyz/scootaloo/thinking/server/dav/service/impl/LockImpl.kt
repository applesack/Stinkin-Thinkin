package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.obj
import xyz.scootaloo.thinking.lang.EventbusMessageHelper
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.lang.set
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.DAVLockService
import xyz.scootaloo.thinking.server.dav.util.JsonToXml

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
        TODO("Not yet implemented")
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.lock) {
            TODO()
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
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val lock = "$prefix:lock"
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

    private object Resolver {

    }

    private object Lock : EventbusMessageHelper {
        fun handle(request: Message<JsonObject>) {
            TODO()
        }
    }

}