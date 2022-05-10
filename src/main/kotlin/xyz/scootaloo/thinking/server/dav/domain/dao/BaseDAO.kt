package xyz.scootaloo.thinking.server.dav.domain.dao

import org.ktorm.database.Database

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 19:53
 */
abstract class BaseDAO {

    lateinit var db: Database

    companion object {


        fun initRef(ref: Database) {

        }
    }
}