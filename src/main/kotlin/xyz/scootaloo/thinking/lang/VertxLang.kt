package xyz.scootaloo.thinking.lang

import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.awaitResult
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * @author flutterdash@qq.com
 * @since 2022/5/2 23:48
 */

infix fun <T> Promise<T>.complete(value: T?) {
    this.complete(value)
}

inline fun <T, R> Future<T>.trans(crossinline lazy: (T) -> R): Future<R> {
    return this.transform { done ->
        if (done.succeeded()) {
            Future.succeededFuture(lazy(done.result()))
        } else {
            Future.failedFuture(done.cause())
        }
    }
}

suspend fun <T> awaitParallelBlocking(block: () -> T): T {
    return awaitResult { handler ->
        val ctx = Vertx.currentContext()
        ctx.executeBlocking<T>({ fut ->
            fut complete block()
        }, false, { ar ->
            handler.handle(ar)
        })
    }
}

private class VertxStandardUnitTest : TestDsl {

    @Test
    fun testAwaitParallelBlocking(): Unit = runBlocking {
        val vertx = Vertx.vertx()
        vertx.deployVerticle(Verticle).await()
    }

    object Verticle : CoroutineVerticle(), TestDsl {
        override suspend fun start() {
            val start = currentTimeMillis()
            val job1 = async {
                displayThread("begin 111").log()
                val t1 = awaitParallelBlocking {
                    Thread.sleep(3000)
                    "111"
                }
                displayThread(t1).log()
            }
            val job2 = async {
                displayThread("begin 222").log()
                val t2 = awaitParallelBlocking {
                    Thread.sleep(2000)
                    "222"
                }
                displayThread(t2).log()
            }
            job1.await()
            job2.await()
            println(currentTimeMillis() - start)
        }

        private fun displayThread(msg: String): String {
            return "[${Thread.currentThread().name}] $msg"
        }
    }

}