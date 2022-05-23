package xyz.scootaloo.thinking.util

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.impl.LRUCache
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.complete
import xyz.scootaloo.thinking.lang.ifNotNull
import xyz.scootaloo.thinking.lang.wrapInFut
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * 惰性缓存池
 *
 * @author flutterdash@qq.com
 * @since 2022/5/22 13:31
 */
class LazyCachePool<K : Any, V : Any>(
    private val vertx: Vertx,
    private val config: LazyCachePoolConfig<K, V>,
) {
    init {
        checkRange()
    }

    private val primary = CountableLRUCache<K, V>(config.run { maxSize percent factor }, config::valueCounter)
    private val secondary = LRUCache<K, Int>(config.run { maxSize percent (1 - factor) })
    private val tasks = LazyAsyncTasks(vertx, config::calculate)

    fun get(item: K): Future<V?> {
        if (item in primary) {
            return primary[item]!!.wrapInFut()
        }
        if (item in tasks) {
            return tasks[item]
        }
        return getAndRegister(item)
    }

    private fun getAndRegister(item: K): Future<V?> {
        return if (item in secondary) {
            val record = secondary[item]!!
            if (record < config.level) {
                secondary[item] = record + 1
                asyncGet(item)
            } else {
                secondary.remove(item)
                addToTasksAndGet(item)
            }
        } else {
            secondary[item] = 1
            asyncGet(item)
        }
    }

    private fun addToTasksAndGet(item: K): Future<V?> {
        return if (item in tasks) {
            tasks[item]
        } else {
            tasks[item].onSuccess { value ->
                value?.ifNotNull { addToCacheAndCirculate(item, it) }
            }
        }
    }

    private fun addToCacheAndCirculate(item: K, value: V) {
        primary.put(item, value).ifNotNull { disuses ->
            for ((disuse, _) in disuses) {
                secondary[disuse] = config.level
            }
        }
    }

    private fun asyncGet(item: K): Future<V?> {
        return vertx.executeBlocking({
            it complete config.calculate(item)
        }, false)
    }

    private fun checkRange() {
        if (config.level < 1) {
            throw java.lang.IllegalArgumentException("level must more then 1")
        }
    }

    private infix fun Int.percent(percent: Double): Int {
        return (this.toDouble() * percent).toInt()
    }
}

interface LazyCachePoolConfig<K, V> {
    val maxSize: Int
    val level: Int
    val factor: Double get() = 0.75

    fun valueCounter(value: V): Int

    fun calculate(key: K): V?
}

private class LazyCachePoolUnitTest : TestDsl {

    @Test
    fun testPresent() {
        ((2000).toDouble() * 0.75).toInt().log()
        ((2000).toDouble() * 0.25).toInt().log()
    }

    @Test
    fun test(): Unit = runBlocking {
        val nums = intArrayOf(
            1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4,
            1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4,
            1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4,
            5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25
        )
        val vertx = Vertx.vertx()
        // 缓存容量4, 候选项容量4
        // nums中是1到10的数组, 其中1到4的出现频率是其他的数的概率的三倍
        // 现在随机访问数组下标1000次, 每次以拿到的值获取缓存, 模拟用户有偏好的访问一些内容
        // 观察获取缓存的用时
        val lazyCache = LazyCachePool(vertx, TestConfig1)
        repeat(1000) {
            val num = nums[Random.nextInt(0, nums.size)]
            launch {
                val interval = measureTimeMillis {
                    lazyCache.get(num).await()
                }
                println("第${it}次, 访问$num, 用时${interval}ms")
            }
        }
    }

    @Test
    fun simpleTest(): Unit = runBlocking {
        val vertx = Vertx.vertx()
        val lazyCache = LazyCachePool(vertx, TestConfig1)
        val nums = intArrayOf(
            1, 1, 1, 2, 2, 2, 1, 2,
            1, 1, 1, 2, 2, 2, 1, 2,
            1, 1, 1, 2, 2, 2, 1, 2,
            1, 1, 1, 2, 2, 2, 1, 2
        )
        for (num in nums) {
            launch {
                val interval = measureTimeMillis {
                    lazyCache.get(num).await()
                }
                println("访问$num, 用时${interval}ms")
            }
        }
    }

    @Test
    fun testThreadSleep() {
        repeat(30) {
            val interval = measureTimeMillis {
                Thread.sleep(3000)
            }
            println("用时${interval}ms")
        }
    }

    object TestConfig1 : LazyCachePoolConfig<Int, String> {
        override val maxSize = 8
        override val level = 1
        override val factor = 0.5

        override fun calculate(key: Int): String {
            display("async $key")
            Thread.sleep(3000)
            return randomString().apply {
//                display("return $this")
            }
        }

        override fun valueCounter(value: String): Int {
            return 1
        }

        private fun randomString(): String {
            val buff = StringBuilder()
            repeat(8) {
                buff.append(Random.nextInt(0, 9))
            }
            return buff.toString()
        }

        private fun display(msg: String) {
            println("[${Thread.currentThread().name}] $msg")
        }
    }

}