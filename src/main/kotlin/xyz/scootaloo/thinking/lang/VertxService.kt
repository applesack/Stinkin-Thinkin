package xyz.scootaloo.thinking.lang

import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.file.FileSystem
import io.vertx.kotlin.core.vertxOptionsOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import xyz.scootaloo.thinking.server.component.CrontabService
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess

/**
 * vertx开发最佳实践
 *
 * 首先, 有一组服务[VertxService], 每个服务都提供一组功能, 服务可以是单例也可以是多例的,
 * 为了统一这种差异, 每个服务都需要提供一个工厂方法[Factory],
 * 由工厂方法的实现来决定服务的实例是单例还是多例[VertxService.SingletonFactory];
 *
 * 然后, 每个服务通常需要运行在特定的上下文中[VertxEventbusConsumerService.context],
 * 为了可以跨上下文访问服务, 这些服务还需要在eventbus上注册监听, 以处理来自其他上下文的调用;
 *
 *
 * -------------------------------------------
 * 如何把这些服务组织起来:
 *
 * 服务的装配要靠verticle, 在这里verticle称为[VertxServiceRegisterCenter], 因为它主要负责装配服务,
 * 同时, 需要有一个地方来管理系统中所有的服务和注册中心[VertxServerDetailedList], 它描述了一个服务的所有主要组件;
 *
 *
 * -------------------------------------------
 * 最后, 名词解释:
 *
 * [VertxService] 服务的基本结构, 系统内部的服务都需要实现这个接口
 * [VertxEventbusConsumerService] 能够处理总线消息的服务, 它的总线处理器会被注册到指定的上下文环境中
 * [VertxServiceRegisterCenter] 注册中心, 用于注册和管理服务
 * [VertxServerDetailedList] 服务的组件清单, 有了它, 可以直接按照配置启动一个服务器
 *
 * @author flutterdash@qq.com
 * @since 2022/5/2 23:14
 */

interface VertxUtils

interface VertxService {

    var vertx: Vertx

    val eb: EventBus get() = vertx.eventBus()

    val fs: FileSystem get() = vertx.fileSystem()

    var entrance: CoroutineEntrance

    val order: Int

    suspend fun start() {}

    fun stop() {}

    fun crontab(): VertxCrontab? = null

    abstract class SingletonFactory<T : VertxService>(private val instance: T) : Factory<String, T> {
        override fun invoke(input: String): T {
            return instance
        }

        operator fun invoke(): T {
            return instance
        }

        fun factory(): Factory<String, VertxService> = Factory { instance }
    }

    abstract class MultiInstanceFactory<T : VertxService>(
        private val lazy: (String) -> T,
    ) : Factory<String, T> {
        private val instanceMapper = ConcurrentHashMap<String, T>()
        override operator fun invoke(input: String): T {
            return instanceMapper[input] ?: synchronized(this) {
                val instance = instanceMapper[input]
                if (instance != null)
                    return instance

                val newInstance = lazy(input)
                instanceMapper[input] = newInstance
                newInstance
            }
        }

        fun factory(): Factory<String, VertxService> = Factory(::invoke)
    }

}

interface VertxEventbusConsumerService : VertxService {

    override val order: Int get() = 5

    val context: String

    fun registerEventbusConsumer(contextName: String)

}

abstract class SingletonVertxService : VertxEventbusConsumerService {

    override lateinit var vertx: Vertx

    override lateinit var entrance: CoroutineEntrance

    override fun registerEventbusConsumer(contextName: String) {}

    fun <T> EventBus.coroutineConsumer(
        address: String, handler: suspend CoroutineScope.(Message<T>) -> Unit,
    ) {
        consumer<T>(address) { message ->
            entrance {
                handler(message)
            }
        }
    }

}

abstract class VertxServiceRegisterCenter : CoroutineVerticle() {
    abstract val log: Logger
    abstract val contextName: String

    fun deploymentId(): String {
        return deploymentID
    }

    fun startCoroutine(block: CoroutineBlock) = launch { block() }

    protected fun closeServer() {
        vertx.close().onComplete {
            exitProcess(0)
        }
    }

    protected fun initServices(details: VertxServerDetailedList): List<VertxService> {
        val serviceFactories = details.listServices()
        val instances = serviceFactories.map { it(contextName) }

        instances.forEach { it.vertx = vertx }
        instances.filterIsInstance(VertxEventbusConsumerService::class.java)
            .filter { it.context like this.contextName }
            .forEach(::configComponent)

        startCrontab()
        return instances
    }

    private fun configComponent(service: VertxEventbusConsumerService): Unit = try {
        service.entrance = this::startCoroutine
        service.crontab().ifNotNull(::registerCrontab)
        service.registerEventbusConsumer(contextName)
        if (service !is CrontabService) {
            startCoroutine {
                service.start()
            }
        }
        Unit
    } catch (error: Throwable) {
        log.error("an error when register service; current context: $contextName", error)
    }

    private fun startCrontab() {
        startCoroutine {
            CrontabService(contextName).start()
        }
    }

    private fun registerCrontab(crontab: VertxCrontab) {
        CrontabService(contextName).submit(crontab)
    }

}

typealias VertxServer = VertxServerDetailedList

abstract class VertxServerDetailedList {

    private var running = false
    private lateinit var vertx: Vertx

    open fun serverVertxOption(): VertxOptions {
        return vertxOptionsOf()
    }

    abstract fun listVerticles(): List<VertxServiceRegisterCenter>

    abstract fun listServices(): List<Factory<String, VertxService>>

    fun isServerRunning(): Boolean = running

    fun bootstrap(): Future<Vertx> {
        return Companion.bootstrap(this).onSuccess {
            this.vertx = it
            running = true
        }
    }

    fun shutdown(): Future<Unit> {
        if (!running) {
            return Future.failedFuture("server not running")
        }
        return Companion.shutdown(this.vertx, this).onSuccess {
            running = false
        }
    }

    companion object {

        fun bootstrap(details: VertxServerDetailedList): Future<Vertx> {
            val vertx = Vertx.vertx(details.serverVertxOption())
            val futList = ArrayList<Future<*>>()
            for (vert in details.listVerticles()) {
                futList add vertx.deployVerticle(vert)
            }
            return CompositeFuture.all(futList).trans { vertx }
        }

        fun shutdown(vertx: Vertx, details: VertxServerDetailedList): Future<Unit> {
            val futList = ArrayList<Future<*>>()
            for (vert in details.listVerticles()) {
                futList add vertx.undeploy(vert.deploymentId())
            }
            return CompositeFuture.all(futList).trans { }
        }

    }

}