package xyz.scootaloo.thinking.server.dav.service

import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.service.impl.FileServiceImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 16:25
 */
interface FileService : VertxService {

    suspend fun viewFiles(path: String, depth: Int): Pair<Boolean, List<AFile>>

    companion object : VertxService.SingletonFactory<FileService>(FileServiceImpl)

}