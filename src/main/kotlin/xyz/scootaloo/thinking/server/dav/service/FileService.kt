package xyz.scootaloo.thinking.server.dav.service

import io.vertx.core.Future
import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.FileServiceImpl
import xyz.scootaloo.thinking.server.dav.util.PathUtils

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 16:25
 */
interface FileService : VertxService {

    /**
     * ## 创建文件夹, 返回创建操作的执行情况
     *
     * @param dirRelativePath 要创建的文件夹的相对路径; 这个操作要求[dirRelativePath]所指向的目录不存在,
     * 同时如果[dirRelativePath]有父目录, 那么它的父目录必须存在, 否则创建不成功;
     * **这个路径必须经过[PathUtils.normalize]格式化**;
     *
     * @param force 如果启用这个参数, 当[dirRelativePath]所在的父目录不存在时, 会自动创建;
     *
     * @return 一个[Future]对象, 其中包含了创建操作的完成情况
     * - 0 创建成功
     * - 1 创建失败: 目标文件已存在
     * - 2 创建失败: 目标位置的父目录不存在
     * - 3 创建失败: 创建的过程中出现内部错误
     */
    fun createDirectory(dirRelativePath: String, force: Boolean = false): Future<Int>

    companion object : VertxService.SingletonFactory<FileService>(FileServiceImpl)

}