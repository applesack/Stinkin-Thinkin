package xyz.scootaloo.thinking.server.dav.util

import xyz.scootaloo.thinking.lang.VertxUtils
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.util.CountableLRUCache

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
class FileCachePool(
    private val singleFileMaxSize: Int,
    private val groupFileMaxSize: Int
) : VertxUtils {

    suspend fun getSingle(): AFile {

        TODO()
    }

    suspend fun getGroup(): List<AFile> {
        TODO()
    }

    /**
     * 使缓存失效
     *
     * 当有文件产生更新(属性变动, 例如重命名, 移动, 删除等), 不会修改当前类维护的缓存,
     * 而是直接把修改的项目从缓存中删除, 当下次访问时再从文件系统获取
     */
    fun invalidate(name: String) {
    }


    private abstract class AutoFileCache(private val maxSize: Int = 2000) {
//        private val cache = CountableLRUCache<String, List<AFile>>()
    }

    private class Candidate(
        val path: String,
        var count: Int = 0,
    )

}