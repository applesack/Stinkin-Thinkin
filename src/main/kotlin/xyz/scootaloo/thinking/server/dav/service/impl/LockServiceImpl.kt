package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.obj
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.set
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.DAVLockService
import xyz.scootaloo.thinking.server.dav.util.JsonToXml

/**
 * @author flutterdash@qq.com
 * @since 2022/5/17 23:32
 */
object LockServiceImpl : SingletonVertxService(), DAVLockService {

    override val context = WebDAVContext.file

    override fun displaySupportedLock(): JsonObject {
        return supported
    }

    private object Terminology {
        const val lockEntry = "LockEntry"
        const val lockScope = "LockScope"
        const val lockType = "LockType"
        const val writeLock = "Write"
        const val shared = "Shared"
        const val exclusive = "Exclusive"
    }

    private val supported = Json.obj {
        this[Terminology.lockEntry] = Json.array {
            add(Json.obj {
                this[Terminology.lockScope] = Json.obj {
                    this[Terminology.exclusive] = JsonToXml.closedTag()
                }
                this[Terminology.lockType] = Json.obj {
                    this[Terminology.writeLock] = JsonToXml.closedTag()
                }
            })
            add(Json.obj {
                this[Terminology.lockScope] = Json.obj {
                    this[Terminology.shared] = JsonToXml.closedTag()
                }
                this[Terminology.lockType] = Json.obj {
                    this[Terminology.writeLock] = JsonToXml.closedTag()
                }
            })
        }
    }

}