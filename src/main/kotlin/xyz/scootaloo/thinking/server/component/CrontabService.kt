package xyz.scootaloo.thinking.server.component

import xyz.scootaloo.thinking.lang.*
import java.util.*

/**
 * @author flutterdash@qq.com
 * @since 2022/5/4 19:07
 */
interface CrontabService : VertxService {

    /**
     * 提交定时任务
     */
    fun submit(crontab: VertxCrontab)

    /**
     * 每个上下文中都可以有一份实例
     */
    companion object : VertxService.MultiInstanceFactory<CrontabService>({ Impl(it) })

    // ------------------------------ Implements ---------------------------------

    private class Impl(override var context: String) : SingletonVertxService(), CrontabService {
        private val interval = 100L
        private val manager = CrontabManager(this)
        val log by lazy { getLogger("$context:crontab") }

        override fun submit(crontab: VertxCrontab) {
            manager.submit(crontab)
        }

        override fun start() {
            if (manager.isNotEmpty()) {
                vertx.setPeriodic(interval) { manager.runTaskQue() }
                log.info("in context $context; crontab service running now," +
                        " that are ${manager.size} task registered;")
            }
        }
    }

    private class CrontabManager(
        private val service: Impl,
        private val taskQue: TreeMap<String, CrontabWrapper> = TreeMap(),
    ) : Map<String, Any> by taskQue {

        fun submit(crontab: VertxCrontab) {
            val wrapper = createCrontabWrapper(crontab)
            taskQue[wrapper.id] = wrapper
        }

        fun runTaskQue() {
            val currentTimeMillis = currentTimeMillis()
            val invalidTaskIds = LinkedList<String>()
            for ((id, wrapper) in taskQue) {
                if (!checkAndRunCrontab(currentTimeMillis, wrapper)) {
                    invalidTaskIds add id
                }
            }

            if (invalidTaskIds.isNotEmpty()) {
                invalidTaskIds.forEach {
                    taskQue remove it
                }
            }
        }

        private fun checkAndRunCrontab(currentTimeMillis: Long, wrapper: CrontabWrapper): Boolean {
            val crontab = wrapper.crontab
            val interval = currentTimeMillis - wrapper.lastExecuteTime
            if (interval > crontab.delay) {
                wrapper.lastExecuteTime = currentTimeMillis
                safeExecuteCrontab(currentTimeMillis, crontab)
            }
            return crontab.valid
        }

        private fun safeExecuteCrontab(currentTimeMillis: Long, crontab: VertxCrontab) = try {
            crontab.run(currentTimeMillis)
        } catch (error: Throwable) {
            service.log.error("crontab execute error, id: ${crontab.id}", error)
        }

        private fun createCrontabWrapper(crontab: VertxCrontab): CrontabWrapper {
            val crontabId = "${suitableOrder(crontab.order)}${crontab.id}"
            return CrontabWrapper(crontabId, currentTimeMillis(), crontab)
        }

        private fun suitableOrder(order: Int): Int {
            if (order > 9) return 9
            if (order < 1) return 1
            return order
        }
    }

    private class CrontabWrapper(
        val id: String,
        var lastExecuteTime: Long,
        val crontab: VertxCrontab,
    )

}