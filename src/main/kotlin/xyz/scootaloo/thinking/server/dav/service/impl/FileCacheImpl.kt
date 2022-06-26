package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.Constant
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.ValueHolder
import xyz.scootaloo.thinking.lang.ifNotNull
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.domain.core.File
import xyz.scootaloo.thinking.server.dav.service.FileCacheService
import xyz.scootaloo.thinking.server.dav.service.FileTreeService
import xyz.scootaloo.thinking.server.dav.service.impl.util.Helper
import xyz.scootaloo.thinking.util.LazyCachePoolConfig
import java.util.*

/**
 * @author flutterdash@qq.com
 * @since 2022/6/3 0:40
 */
object FileCacheImpl : SingletonVertxService(), FileCacheService {
    override val context = WebDAVContext.file

    private val fileTree by lazy { FileTreeService() }

    override suspend fun getSingle(relativeFilePath: String): ValueHolder<AFile> {
        val result = SingleFileCache.get(relativeFilePath).await()
        return ValueHolder(result)
    }

    override suspend fun getGroup(relativeDirPath: String): ValueHolder<List<AFile>> {
        val result = GroupFileCache.get(relativeDirPath).await()
        return ValueHolder(result)
    }

    private val SingleFileCache by lazy { SingleFileCacheConfig.createCacheInstance(vertx) }

    private object SingleFileCacheConfig : LazyCachePoolConfig<String, AFile> {
        override val maxSize = 400
        override val level = 3

        /**
         * 查询一个文件
         *
         * @param key 相对路径
         */
        override fun blockingCalculate(key: String): AFile? {
            return solveSingleFile(key)
        }

        override fun valueCounter(value: AFile): Int {
            return 1
        }
    }

    private val GroupFileCache by lazy { GroupFileCacheConfig.createCacheInstance(vertx) }

    private object GroupFileCacheConfig : LazyCachePoolConfig<String, List<AFile>> {
        override val maxSize = 2000
        override val level = 2

        /**
         * 查询一个文件夹内的所有文件
         *
         * @param key 相对路径
         */
        override fun blockingCalculate(key: String): List<AFile>? {
            val dirFullPath = Helper.fullPath(key)
            val dirExists = fs.existsBlocking(dirFullPath)
            if (!dirExists) {
                return null
            }

            val dirProps = fs.propsBlocking(dirFullPath)
            if (!dirProps.isDirectory) {
                return null
            }

            val content = fs.readDirBlocking(dirFullPath)
            val result = LinkedList<AFile>()
            for (file in content) {
                solveSingleFileFullPath(file).ifNotNull {
                    result.add(it)
                }
            }

            return result
        }

        override fun valueCounter(value: List<AFile>): Int {
            return value.size
        }
    }

    private fun solveSingleFile(filepath: String): AFile? {
        val fullPath = Helper.fullPath(filepath)
        return solveSingleFileFullPath(fullPath)
    }

    private fun solveSingleFileFullPath(fullPath: String): AFile? {
        val exists = fs.existsBlocking(fullPath)
        if (!exists) {
            return null
        }
        val props = fs.propsBlocking(fullPath)
        return File.build(fullPath, fileTree.basePath(), Constant.UNKNOWN, props)
    }

}