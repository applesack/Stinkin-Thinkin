package xyz.scootaloo.thinking.server.component

import xyz.scootaloo.thinking.lang.Authenticator
import xyz.scootaloo.thinking.lang.UserProvider
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.component.impl.DigestAuthServiceImpl

/**
 * http 摘要加密实现
 *
 * @author flutterdash@qq.com
 * @since 2022/5/6 13:01
 */
interface DigestAuthService : VertxService, Authenticator {

    fun setProvider(provider: UserProvider)

    companion object : VertxService.SingletonFactory<DigestAuthService>(DigestAuthServiceImpl)

}