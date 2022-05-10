package xyz.scootaloo.thinking.server.dav.application

import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import org.ktorm.database.Database
import xyz.scootaloo.thinking.lang.VertxServiceRegisterCenter
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.dav.WebDAVServer

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
        if (exists) {
            val props = fs.props(db).await()
            if (props.isDirectory) {
                log.error("database file '$db' not a file")
                return closeServer()
            }
        } else {
            fs.mkdirs("./conf").await()
            fs.createFile(db).await()
            val database = safeCreateDatabaseRef() ?: return closeServer()
            initDatabaseStruct(fs, database)
            WebDAVContext.database = database
        }
        log.info("database available now")
    }

    private fun safeCreateDatabaseRef(): Database? = try {
        Database.connect(
            driver = "org.sqlite.JDBC",
            url = "jdbc:sqlite:$db"
        )
    } catch (e: Throwable) {
        log.error("an error when create database instance", e)
        null
    }

    private suspend fun initDatabaseStruct(fs: FileSystem, database: Database) {
        val sql = safeLoadInitScript(fs) ?: return closeServer()
        database.useConnection { conn ->
            conn.prepareStatement(sql).execute()
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