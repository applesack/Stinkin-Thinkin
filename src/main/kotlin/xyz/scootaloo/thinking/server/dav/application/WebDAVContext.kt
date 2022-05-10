package xyz.scootaloo.thinking.server.dav.application

import org.ktorm.database.Database
import xyz.scootaloo.thinking.lang.ContextRegedit
import xyz.scootaloo.thinking.lang.VariableHolder

/**
 * @author flutterdash@qq.com
 * @since 2022/5/5 12:48
 */

object WebDAVContext : ContextRegedit() {
    const val file = "file"

    lateinit var database: Database
}