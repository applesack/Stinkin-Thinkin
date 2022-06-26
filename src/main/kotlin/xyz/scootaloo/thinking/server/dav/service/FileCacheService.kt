package xyz.scootaloo.thinking.server.dav.service

import xyz.scootaloo.thinking.lang.ValueHolder
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.service.impl.FileCacheImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/27 12:20
 */
interface FileCacheService : VertxService {

    suspend fun getSingle(relativeFilePath: String): ValueHolder<AFile>

    suspend fun getGroup(relativeDirPath: String): ValueHolder<List<AFile>>

    companion object : VertxService.SingletonFactory<FileCacheService>(FileCacheImpl)

}