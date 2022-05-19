package xyz.scootaloo.thinking.server.dav.service

import xyz.scootaloo.thinking.lang.VertxService
import xyz.scootaloo.thinking.server.dav.service.impl.PropPatchImpl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/18 22:51
 */
interface DAVPropPatchService : VertxService {


    companion object : VertxService.SingletonFactory<DAVPropPatchService>(PropPatchImpl)

}