package xyz.scootaloo.thinking.server.dav.application

import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import org.ktorm.database.Database
import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.dav.WebDAVServer
import java.sql.Connection
import java.sql.DriverManager
import kotlin.concurrent.thread

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 19:34
 */
object WebDAVStateVerticle : VertxServiceRegisterCenter() {
    override val log by lazy { getLogger("state") }
    override val contextName = WebDAVContext.state

    override suspend fun start() {
        initServices(WebDAVServer)
        prepareDatabase()
    }

    private const val db = "./conf/data.sqlite"

    private suspend fun prepareDatabase() {
        val fs = vertx.fileSystem()
        val exists = fs.exists(db).await()
        val database: Database
        if (exists) {
            val props = fs.props(db).await()
            if (props.isDirectory) {
                log.error("database file '$db' not a file")
                return closeServer()
            } else {
                database = safeCreateDatabaseRef() ?: return closeServer()
            }
        } else {
            fs.mkdirs("./conf").await()
//            fs.createFile(db).await()
            database = safeCreateDatabaseRef() ?: return closeServer()
            initDatabaseStruct(fs, database)
        }
        WebDAVContext.database = database
        log.info("database available now")
    }

    private fun safeCreateDatabaseRef(): Database? = try {
        // https://www.ktorm.org/zh-cn/connect-to-databases.html
        Class.forName("org.sqlite.JDBC")
        val conn = DriverManager.getConnection("jdbc:sqlite:$db")
        Runtime.getRuntime().addShutdownHook(
            thread(start = false) {
                conn.close()
            }
        )
        Database.connect {
            object : Connection by conn {
                override fun close() {
                }
            }
        }
    } catch (e: Throwable) {
        log.error("an error when create database instance", e)
        null
    }

    private suspend fun initDatabaseStruct(fs: FileSystem, database: Database): Any? {
        return try {
            val sql = safeLoadInitScript(fs) ?: return closeServer()
            database.useConnection { conn ->
                val updated = conn.prepareStatement(sql).executeUpdate()
                log.info("updated item $updated")
            }
        } catch (error: Throwable) {
            log.error("an error when execute sql init script", error)
            null
        }
    }

    private suspend fun safeLoadInitScript(fs: FileSystem): String? = try {
        val buff = fs.readFile("init.sql").await()
        buff.getString(0, buff.length())
    } catch (error: Throwable) {
        log.error("an error when load init database script", error)
        null
    }

}