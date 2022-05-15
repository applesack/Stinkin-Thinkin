package xyz.scootaloo.thinking.server.component

import xyz.scootaloo.thinking.lang.VertxCrontab
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.component.impl.CrontabServiceImpl

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
    companion object : VertxService.MultiInstanceFactory<CrontabService>({ CrontabServiceImpl(it) })

}