package xyz.scootaloo.thinking.server.dav.domain.core

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/26 11:16
 */

enum class State(private val code: Int) {
    HIT(1),
    REFUSE(1 shl 1),
    PASS(1 shl 2),
    CONFLICT(1 shl 3),
    UNMAPPING(1 shl 4),
    ;

    companion object {
        fun merge(vararg states: State): Int {
            var result = 0
            for (state in states) {
                result = result or state.code
            }
            return result
        }

        /**
         * @return
         * 如果返回true, 表示[state]在[set]内, 否则不在
         */
        fun contains(set: Int, state: State): Boolean {
            return (state.code and set) != 0
        }
    }
}

class Pass(val token: String)

private class StateUnitTest : TestDsl {
    @Test
    fun test() {
        val states = arrayOf(State.REFUSE, State.PASS, State.CONFLICT)
        val set = State.merge(*states)
        State.contains(set, State.HIT).log()
        State.contains(set, State.PASS).log()
    }
}
