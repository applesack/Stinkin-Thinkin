package xyz.scootaloo.thinking.server.dav.domain.dao

import org.ktorm.dsl.eq
import org.ktorm.entity.find
import xyz.scootaloo.thinking.server.dav.domain.UserRecordEntity
import xyz.scootaloo.thinking.server.dav.domain.users

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 20:02
 */
object UserDAO : BaseDAO() {

    fun findById(uid: Int): UserRecordEntity? {
        return db.users.find { it.id eq uid }
    }

    fun findByName(name: String): UserRecordEntity? {
        return db.users.find { it.username eq name }
    }

}