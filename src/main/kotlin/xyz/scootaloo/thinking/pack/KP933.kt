package xyz.scootaloo.thinking.pack

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 12:42
 */
class KP933 : TestDsl {

    class RecentCounter {

        private val que = ArrayDeque<Int>()

        fun ping(t: Int): Int {
            que.addLast(t)
            while (true) {
                val head = que.first()
                if (t - head > 3000) {
                    que.removeFirst()
                } else {
                    break
                }
            }
            return que.size
        }

    }

    @Test
    fun test() {
        val counter = RecentCounter()
        counter.ping(1) shouldBe 1
        counter.ping(100) shouldBe 2
        counter.ping(3001) shouldBe 3
        counter.ping(3002) shouldBe 3
    }

}