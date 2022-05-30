package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.FileService
import xyz.scootaloo.thinking.server.dav.service.fs.VirtualFileSystem
import java.nio.file.NotDirectoryException

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 20:13
 */
object FileServiceImpl : SingletonVertxService(), FileService {
    private val log by lazy { getLogger("file") }

    override val context = WebDAVContext.file

    override fun createDirectory(dirRelativePath: String, force: Boolean): Future<Int> {
        return eb.request<Int>(InternalProtocol.createDir, Json.obj {
            this[Constant.SUBJECT] = dirRelativePath
            this[Constant.FORCE] = force
        }).trans { it.body() }
    }

    override suspend fun start() {
        scanHome()
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.createDir) {
            val filename = it.body().getString(Constant.SUBJECT) ?: "/"
            val force = it.body().getBoolean(Constant.FORCE) ?: false
            val result = VirtualFileSystem.createDirectory(filename, force, fs)
            it.reply(result)
        }

        log.info("eventbus 'File' service ready; current context: $contextName")
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

    private object InternalProtocol {
        private const val prefix = "sys:file"
        const val createDir = "$prefix:createDir"
    }

}