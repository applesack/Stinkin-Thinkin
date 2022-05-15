package xyz.scootaloo.thinking.server.component

import io.vertx.core.Future
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.component.impl.CacheServiceImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/4 20:01
 */
interface CacheService : VertxService {

    fun get(key: String): Future<String?>

    fun put(key: String, value: String, expiryTime: Long = defTime): Future<Unit>

    fun delete(key: String): Future<Unit>

    fun updateKeyExpiry(key: String, newExpiryTime: Long = defTime): Future<Unit>

    companion object : VertxService.SingletonFactory<CacheService>(CacheServiceImpl) {
        const val defTime = 5000L
        const val invalidTime = 0L
    }

}