package xyz.scootaloo.thinking.server.dav.service.internal

import io.vertx.ext.web.impl.LRUCache
import xyz.scootaloo.thinking.lang.Context
import xyz.scootaloo.thinking.lang.VertxUtils
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.AFile

/**
 * 文件缓存
 *
 * 缓存最常浏览的文件的信息, 避免频繁访问文件系统;
 *
 * --------------------------------
 *
 * @author flutterdash@qq.com
 * @since 2022/5/18 15:44
 */
@Context(WebDAVContext.file)
object FileCache : VertxUtils {

    private val cache = LRUCache<String, String>(700)

    suspend fun getSingle(): AFile {

        TODO()
    }

    suspend fun getGroup(): List<AFile> {
        TODO()
    }

    private class CacheItem(

    )

}