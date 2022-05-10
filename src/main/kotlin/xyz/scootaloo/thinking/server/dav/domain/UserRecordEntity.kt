package xyz.scootaloo.thinking.server.dav.domain

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import xyz.scootaloo.thinking.lang.User

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 18:09
 */
interface UserRecordEntity : Entity<UserRecordEntity>, User {
    override var id: Int
    override var username: String
    override var password: String
    override var role: Int
    var created: Long

    companion object : Entity.Factory<UserRecordEntity>()
}

object UserRecordEntities : Table<UserRecordEntity>("users")


val Database.users get() = sequenceOf(UserRecordEntities)

