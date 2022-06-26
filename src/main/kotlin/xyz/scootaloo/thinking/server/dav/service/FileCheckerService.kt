package xyz.scootaloo.thinking.server.dav.service

import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.FileCheckerServiceImpl
import java.util.concurrent.Future

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 16:25
 */
interface FileCheckerService : VertxService {

    fun detect(path: String, ifExpr: String?): Future<Int>

    companion object : VertxService.SingletonFactory<FileCheckerService>(FileCheckerServiceImpl)

}