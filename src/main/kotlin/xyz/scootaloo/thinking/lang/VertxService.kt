package xyz.scootaloo.thinking.lang

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.CoroutineVerticle

/**
 * vertx开发最佳实践
 *
 * 一个vertx服务器由多个verticle组成, 每个verticle可以管理一组服务, 这些服务通过eventbus供客户端调用,
 * 同时服务需要运行在特定的上下文中,
 *
 * @author flutterdash@qq.com
 * @since 2022/5/2 23:14
 */

interface VertxService {

    val vertx: Vertx

    val eb: EventBus get() = vertx.eventBus()

    val fs: FileSystem get() = vertx.fileSystem()

}

interface VertxEventbusConsumerService : VertxService {

    val context: String

    fun registerEventbusConsumer()

}

abstract class SingletonVertxService : VertxEventbusConsumerService {

    override lateinit var vertx: Vertx

    override lateinit var context: String

    override fun registerEventbusConsumer() {}

}

abstract class VertxServiceRegisterCenter : CoroutineVerticle() {

    abstract val contextName: String

}

interface VertxServerDetailedList {

    fun list(): List<VertxServiceRegisterCenter>

}