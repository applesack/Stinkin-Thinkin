package xyz.scootaloo.thinking.server.dav.domain

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import xyz.scootaloo.thinking.lang.Constant
import xyz.scootaloo.thinking.lang.User
import xyz.scootaloo.thinking.lang.ifNotNull

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 18:09
 */
interface UserRecordEntity : Entity<UserRecordEntity>, User {
    override var id: Int
    override var username: String
    override var password: String
    var creationDate: Long

    companion object : Entity.Factory<UserRecordEntity>() {
        fun of(json: JsonObject): UserRecordEntity {
            return invoke {
                username = json[Constant.USERNAME]
                password = json[Constant.PASSWORD]
                json.getValue(Constant.ID).ifNotNull {
                    if (it is Int) {
                        id = it
                    }
                }

                json.getValue(Constant.CREATION_DATE).ifNotNull {
                    if (it is Long) {
                        creationDate = it
                    }
                }
            }
        }
    }
}

object UserRecordEntities : Table<UserRecordEntity>("users") {
    val id = int("id").bindTo { it.id }.primaryKey()
    val username = varchar("username").bindTo { it.username }
    val password = varchar("password").bindTo { it.password }
    val creationDate = long("creationDate").bindTo { it.creationDate }
}


val Database.users get() = sequenceOf(UserRecordEntities)

