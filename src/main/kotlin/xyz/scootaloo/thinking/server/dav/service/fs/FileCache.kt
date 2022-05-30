package xyz.scootaloo.thinking.server.dav.service.fs

import io.vertx.core.file.FileSystem
import kotlinx.coroutines.delay
import xyz.scootaloo.thinking.lang.StateValueHolder
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.domain.core.State

/**
 * @author flutterdash@qq.com
 * @since 2022/5/27 12:20
 */
object FileCache {

    suspend fun getSingle(fullFilePath: String, fs: FileSystem): StateValueHolder<State, AFile> {
        delay(200)
        TODO()
    }

    fun getGroup(dir: String): List<StateValueHolder<State, AFile>> {
        TODO()
    }

}