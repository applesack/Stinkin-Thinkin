package xyz.scootaloo.thinking.server.dav.domain.dao

import org.ktorm.dsl.eq
import org.ktorm.entity.find
import xyz.scootaloo.thinking.server.dav.domain.FileRecordEntity
import xyz.scootaloo.thinking.server.dav.domain.files

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 20:03
 */
object FileDAO : BaseDAO() {

    fun findRecord(path: String): FileRecordEntity? {
        return db.files.find { it.path eq path }
    }

}