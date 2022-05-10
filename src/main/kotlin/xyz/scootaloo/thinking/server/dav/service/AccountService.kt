package xyz.scootaloo.thinking.server.dav.service

import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 20:27
 */
interface AccountService : VertxService, UserProvider {

    fun register(username: String, password: String): Future<Unit>

    override fun findByName(username: String): Future<User?>

    companion object : VertxService.SingletonFactory<AccountService>(Impl)

    // ------------------------------ Implements ---------------------------------

    private object InternalProtocol {
        private const val prefix = "sys:user"
        const val register = "$prefix:register"
        const val findUserByName = "$prefix:findUserByName"
    }

    private object Generic {
        const val username = "username"
        const val password = "password"
    }

    private object Impl : SingletonVertxService(), AccountService {
        private val log by lazy { getLogger("account") }
        override var context: String = WebDAVContext.state

        override fun register(username: String, password: String): Future<Unit> {
            return eb.callService(InternalProtocol.register, Json.obj {
                this[Generic.username] = username
                this[Generic.password] = password
            }).trans { }
        }

        override fun findByName(username: String): Future<User?> {
            TODO("Not yet implemented")
        }

        override fun registerEventbusConsumer(contextName: String) {
            eb.coroutineConsumer<JsonObject>(InternalProtocol.register) {

            }
        }
    }

    private object UserManager {
        fun register(username: String, password: String) {

        }
    }

}