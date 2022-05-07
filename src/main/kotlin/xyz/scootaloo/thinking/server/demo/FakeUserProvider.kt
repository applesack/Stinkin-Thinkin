package xyz.scootaloo.thinking.server.demo

import io.vertx.core.Future
import xyz.scootaloo.thinking.lang.User
import xyz.scootaloo.thinking.lang.UserProvider

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 18:56
 */
object FakeUserProvider : UserProvider {
    override fun findByName(username: String): Future<User?> {
        return Future.succeededFuture(EUser(0, username))
    }

    class EUser(
        override val id: Int = 0,
        override val username: String,
        override val password: String = "123456",
        override val role: Int = 7,
    ) : User
}