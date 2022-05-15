package xyz.scootaloo.thinking.server.dav.domain

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 18:08
 */

interface FileRecordEntity : Entity<FileRecordEntity> {

    val path: String
    val creationDate: Long
    val download: Int
    val author: Int

    companion object : Entity.Factory<FileRecordEntity>()

}

object FileRecordEntities : Table<FileRecordEntity>("files") {

    val path = varchar("path").bindTo { it.path }.primaryKey()
    val creationDate = long("creationDate").bindTo { it.creationDate }
    val download = int("download").bindTo { it.download }
    val author = int("author").bindTo { it.author }

}

val Database.files get() = sequenceOf(FileRecordEntities)