package xyz.scootaloo.thinking.sample

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import java.util.*
import kotlin.concurrent.thread

/**
 * 异步任务串行化
 *
 * @author flutterdash@qq.com
 * @since 2022/5/3 19:34
 */
private object AsyncTaskSerialization : CoroutineVerticle() {

    private var number = 0
    private val que = LinkedList<Pair<Int, RoutingContext>>()

    private val threadName get() = Thread.currentThread().name

    override suspend fun start() {
        val router = Router.router(vertx)
        router.route().handler { ctx ->
            // 主线程接收点击事件
            println("[主线程 $threadName] 收到点击事件")
            schedule(ctx)
            number++
        }

        // 主线程注册消息处理器
        vertx.eventBus().consumer<String>("done") {
            println("[主线程 $threadName] 任务完成后通知主线程更新队列")
            if (que.isNotEmpty())
                que.removeFirst()
            if (que.isNotEmpty())
                execute()
        }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080).await()
    }

    // 主线程代码: 提交任务
    private fun schedule(ctx: RoutingContext) {
        que.addLast(number to ctx)
        if (que.size > 1)
            return
        execute()
    }

    // 执行任务队列
    private fun execute() {
        if (que.isEmpty())
            return
        val (num, ctx) = que.peekFirst()
        // 创建线程执行任务
        thread {
            println("-------------------")
            println("[子线程 $threadName] 任务开始执行, 编号$num")
            Thread.sleep(5000)
            ctx.end("$num")
            println("[子线程 $threadName] 任务执行完成, 向主线程发送消息, 当前任务编号$num")
            vertx.eventBus().send("done", "")
        }
    }
}

fun main() {
    Vertx.vertx().deployVerticle(AsyncTaskSerialization)
}