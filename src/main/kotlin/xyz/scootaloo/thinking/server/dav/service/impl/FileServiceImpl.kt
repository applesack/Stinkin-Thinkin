package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.FileService
import xyz.scootaloo.thinking.server.dav.service.internal.VirtualFileSystem
import java.nio.file.NotDirectoryException

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 20:13
 */
object FileServiceImpl : SingletonVertxService(), FileService {
    private val log by lazy { getLogger("file") }

    override val context = WebDAVContext.file

    override suspend fun start() {
        scanHome()
    }

    private suspend fun scanHome() {
        val mounted = VirtualFileSystem.basePath
        if (fs.exists(mounted).await()) {
            val props = fs.props(mounted).await()
            if (!props.isDirectory) {
                log.error("vfs init error", NotDirectoryException(mounted))
                return
            }
        } else {
            fs.mkdirs(mounted).await()
        }

        val (dirCount, fileCount) = VirtualFileSystem.initDirectoryStruct(fs)
        log.info("path [$mounted] has been mounted, dir $dirCount, file $fileCount")
    }

}