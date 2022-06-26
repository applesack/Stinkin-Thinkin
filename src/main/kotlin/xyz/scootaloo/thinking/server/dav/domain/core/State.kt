package xyz.scootaloo.thinking.server.dav.domain.core

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.struct.http.IfExpression

/**
 * @author flutterdash@qq.com
 * @since 2022/5/26 11:16
 */

enum class State(private val code: Int) {
    /**
     *## 命中
     *
     * 访问类操作, 例如键值对缓存, 访问文件系统等; 当一个键成功映射到了一个实体, 返回此状态;
     */
    HIT(1 shl 1),

    /**
     * ## 未映射
     *
     * 访问类操作, 和[HIT]相反, 当键无法映射到实体, 返回此状态;
     */
    UNMAPPING(1 shl 2),
    /**
     * ## 拒绝
     *
     * 服务类操作, 客户端请求服务时, 提交的参数信息不足, 导致服务无法执行, 返回此状态;
     */
    REFUSE(1 shl 3),

    /**
     * ## 通过
     *
     * 服务类操作, 当服务成功调用, 返回此状态
     */
    PASS(1 shl 4),

    /**
     * ## 不兼容
     *
     * 服务类操作, 客户端请求服务, 由于提交的参数和服务维护的状态冲突, 返回此状态;
     */
    INCOMPATIBLE(1 shl 5),

    /**
     * ## 已失效
     *
     * 服务类操作, 客户端请求修改一个已经不存在的实体, 返回此状态
     */
    EXPIRED(1 shl 6),
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

class Pass(val token: IfExpression)

private class StateUnitTest : TestDsl {
    @Test
    fun test() {
        val states = arrayOf(State.REFUSE, State.PASS, State.INCOMPATIBLE)
        val set = State.merge(*states)
        State.contains(set, State.HIT).log()
        State.contains(set, State.PASS).log()
    }
}
