package xyz.scootaloo.thinking.server.dav.service.impl.util

import io.vertx.ext.web.impl.LRUCache
import xyz.scootaloo.thinking.lang.VertxUtils
import xyz.scootaloo.thinking.lang.awaitParallelBlocking
import xyz.scootaloo.thinking.lang.ifNotNull
import xyz.scootaloo.thinking.server.dav.domain.UserRecordEntity
import xyz.scootaloo.thinking.server.dav.domain.dao.UserDAO
import java.util.*

/**
 * @author flutterdash@qq.com
 * @since 2022/5/18 10:18
 */
object UserManager : VertxUtils {

    private const val CACHE_CAPACITY = 160
    private val primaryCache: LRUCache<String, UserRecordEntity> = LRUCache(CACHE_CAPACITY)
    private val secondaryCache: LRUCache<Int, UserRecordEntity> = LRUCache(CACHE_CAPACITY)

    suspend fun findByName(name: String): UserRecordEntity? {
        return primaryCache[name] ?: updateCache(asyncFindUserByName(name))
    }

    suspend fun findByIdBatchQuery(ids: List<Int>): List<Pair<Int, UserRecordEntity>> {
        val result = LinkedList<Pair<Int, UserRecordEntity>>()
        for (id in ids) {
            val record = findById(id) ?: continue
            result.add(id to record)
        }
        return result
    }

    suspend fun findById(uid: Int): UserRecordEntity? {
        return secondaryCache[uid] ?: updateCache(asyncFindUserById(uid))
    }

    private fun updateCache(record: UserRecordEntity?): UserRecordEntity? {
        return record.ifNotNull {
            primaryCache[it.username] = record
            secondaryCache[it.id] = it
        }
    }

    private suspend fun asyncFindUserByName(name: String): UserRecordEntity? {
        return awaitParallelBlocking { UserDAO.findByName(name) }
    }

    private suspend fun asyncFindUserById(uid: Int): UserRecordEntity? {
        return awaitParallelBlocking {  UserDAO.findById(uid) }
    }

}