package xyz.scootaloo.thinking.server.dav.service.impl

import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.DAVPropPatchService

/**
 * @author flutterdash@qq.com
 * @since 2022/5/18 22:51
 */
object PropPatchImpl : SingletonVertxService(), DAVPropPatchService {
    override val context = WebDAVContext.file


    private object PropPatch
}