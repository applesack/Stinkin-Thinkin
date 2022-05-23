package xyz.scootaloo.thinking.util

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.complete
import xyz.scootaloo.thinking.lang.putIfAbsent

/**
 *
 * @param R 任务结果类型
 *
 * @author flutterdash@qq.com
 * @since 2022/5/21 15:32
 */
class LazyAsyncTasks<K, R>(private val vertx: Vertx, private val gen: (K) -> R?) {
    private val map = HashMap<K, Future<R?>>()

    operator fun contains(id: K): Boolean {
        return id in map
    }

    operator fun get(id: K): Future<R?> {
        return execute(id)
    }

    fun execute(id: K): Future<R?> {
        return map.putIfAbsent(id) {
            vertx.executeBlocking({
                it complete gen(id)
            }, false)
        }
    }

}


private class LazyAsyncTasksUnitTest : TestDsl {

    @Test
    fun test(): Unit = runBlocking {
        val vertx = Vertx.vertx()
        val tasks = LazyAsyncTasks<String, String>(vertx) { blocking(it) }
        tasks.execute("22222222222")
        tasks.execute("11111111111")
        launch { tasks.execute("11111111111").await().log() }
        launch { tasks.execute("11111111111").await().log() }
        launch { tasks.execute("11111111111").await().log() }
        launch { tasks.execute("11111111111").await().log() }
        launch { tasks.execute("11111111111").await().log() }
        launch { tasks.execute("22222222222").await().log() }
    }

    private fun blocking(value: String): String {
        "run task: value: $value".log()
        Thread.sleep(4000)
        return value
    }

}