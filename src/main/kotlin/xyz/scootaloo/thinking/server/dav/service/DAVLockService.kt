package xyz.scootaloo.thinking.server.dav.service

import io.vertx.core.json.JsonObject
import xyz.scootaloo.thinking.lang.Immutable
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.LockServiceImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/17 23:31
 */
interface DAVLockService : VertxService {

    @Immutable
    fun displaySupportedLock(): JsonObject

    companion object : VertxService.SingletonFactory<DAVLockService>(LockServiceImpl)

}