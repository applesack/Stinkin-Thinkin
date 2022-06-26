package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.UserRecordEntity
import xyz.scootaloo.thinking.server.dav.service.AccountService
import xyz.scootaloo.thinking.server.dav.service.impl.util.UserManager

/**
 * @author flutterdash@qq.com
 * @since 2022/5/17 22:33
 */
object AccountServiceImpl : SingletonVertxService(), AccountService {

    private val log by lazy { getLogger("account") }
    override val context = WebDAVContext.state

    override fun register(username: String, password: String): Future<Unit> {
        return eb.callService(InternalProtocol.register, Json.obj {
            this[Constant.USERNAME] = username
            this[Constant.PASSWORD] = password
        }).trans { }
    }

    override fun findByName(username: String): Future<User?> {
        return eb.callService(InternalProtocol.findUserByName, Json.obj {
            this[Constant.USERNAME] = username
        }).trans {
            val result = it.body()
            if (result != null) {
                return@trans UserRecordEntity.of(result)
            }
            null
        }
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.register) {

        }

        eb.coroutineConsumer<JsonObject>(InternalProtocol.findUserByName) {
            val username = it.body().getString(Constant.USERNAME)
            val result = UserManager.findByName(username)
            it.reply(result?.jsonify())
        }
    }

    private object InternalProtocol {
        private const val prefix = "sys:user"
        const val register = "$prefix:register"
        const val findUserByName = "$prefix:findUserByName"
    }

}