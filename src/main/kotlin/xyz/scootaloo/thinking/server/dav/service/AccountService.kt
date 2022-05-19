package xyz.scootaloo.thinking.server.dav.service

import io.vertx.core.Future
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.service.impl.AccountServiceImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 20:27
 */
interface AccountService : VertxService, UserProvider {

    fun register(username: String, password: String): Future<Unit>

    override fun findByName(username: String): Future<User?>

    companion object : VertxService.SingletonFactory<AccountService>(AccountServiceImpl)

}