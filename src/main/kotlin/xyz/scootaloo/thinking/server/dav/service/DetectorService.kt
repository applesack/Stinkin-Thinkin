package xyz.scootaloo.thinking.server.dav.service

import io.vertx.core.file.FileSystem
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.domain.core.*
import xyz.scootaloo.thinking.server.dav.service.impl.DetectorImpl
import xyz.scootaloo.thinking.server.dav.util.PathUtils

/**
 * @author flutterdash@qq.com
 * @since 2022/6/3 15:11
 */
interface DetectorService : VertxService {

    /**
     * ## 尝试给一个路径加锁
     *
     * ### 什么情况下能加锁成功?
     * 1. 从根节点起, 到目标位置之间的每一个节点都没有锁存在, 则加锁成功;
     * 如果有节点有锁的, 则锁的深度必须是0, 否则加锁失败;
     * 2. 目标位置如果不存在锁, 则加锁成功;
     * 如果已经存在锁的, 假如已有的锁和新的锁兼容, 则成功, 否则不成功;
     *
     * @param subject 文件路径; 必须是经过[PathUtils.normalize]处理过的路径
     * @param lockInfo 尝试给路径加的锁
     * @param pass 令牌, 当路径上存在有锁时可以使用此令牌通行
     * @param fs 文件系统工具, 此操作需要此工具辅助完成
     * @return
     * 加锁操作完成情况, 返回一个三元组;
     * 第一个值是创建锁的状态, 第二个值是实际的锁, 第三个值是子结构中不能覆盖的部分;
     * 只有当第一个值为[State.PASS]时第二个值才可用;
     *
     * 其他状态:
     * - [State.UNMAPPING] 目标实体不存在
     * - [State.INCOMPATIBLE] 锁不兼容
     * - [State.EXPIRED] pass信息缺失或者锁已不存在或者令牌无效
     */
    @kotlin.jvm.Throws(Throwable::class)
    suspend fun tryLock(
        subject: String, lockInfo: LockInfo, pass: Pass?, fs: FileSystem,
    ): Pair<State, FileLock>

    /**
     * ## 给[subject]路径上解锁
     *
     * @return
     * 返回三种状态
     * - [State.UNMAPPING] 未映射, 要解锁的路径不存在
     * - [State.PASS] 解锁成功
     * - [State.REFUSE] 令牌错误, 导致无法解锁
     */
    fun unlock(subject: String, token: String): State

    /**
     * ## 根据[pass]刷新一个锁(给已经存在的锁一个新的过期时间)
     *
     * @return
     * 当锁不存在或者没有提供正确的令牌, 则刷新失败, 返回[State.EXPIRED];
     * 如果刷新成功, 返回[State.PASS]
     */
    fun refreshLock(subject: String, timeout: Long, pass: Pass?): Pair<State, FileLock>

    /**
     * ## 根据提供的[pass], 检查是否有[subject]的访问权限
     *
     * @return
     * 只有两种情况
     * 1. [State.PASS] 允许通过
     * 2. [State.REFUSE] 拒绝访问
     */
    fun evaluate(subject: String, pass: Pass?): State

    /**
     * ## 返回一个监视器对象, 这个对象可以检查某个节点下某条路径是否允许用令牌通过
     */
    fun monitor(path: String): Monitor

    interface Monitor {

        fun evaluate(filename: String, pass: Pass?): State

        fun display(): String

        fun update(newPath: String)

    }

    companion object : VertxService.SingletonFactory<DetectorService>(DetectorImpl)

}