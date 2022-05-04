package xyz.scootaloo.thinking.lang

/**
 * @author flutterdash@qq.com
 * @since 2022/5/4 19:30
 */
interface VertxCrontab {

    /**
     * 可以在[run]方法中修改这个属性, 当这个属性为false时, 当前任务会从定时任务队列中删除(将不会再定期执行)
     */
    val valid: Boolean

    /**
     * 任务标记, 这个名称必须是唯一的(在一个同一个上下文中唯一即可)
     */
    val id: String

    /**
     * 执行任务的间隔(单位毫秒), 可以在[run]方法中动态调整
     */
    val delay: Long

    /**
     * 运行时的优先级, 数字越小优先级越高, 只能取1~9, 默认5
     */
    val order: Int

    /**
     * 定时执行的任务, 这个方法中的代码会EventLoop上下文中执行, 所以尽量避免让线程阻塞
     */
    fun run(currentTimeMillis: Long)

}

abstract class VertxCrontabAdapter : VertxCrontab {
    override val valid: Boolean = true
    override val delay: Long = 100L
    override val order: Int = 5
}