package xyz.scootaloo.thinking.server.dav.domain.dao

import org.ktorm.database.Database
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 19:53
 */
abstract class BaseDAO {

    val db get() = WebDAVContext.database

}