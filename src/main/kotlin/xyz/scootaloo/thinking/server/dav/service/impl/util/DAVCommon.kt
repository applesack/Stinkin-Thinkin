package xyz.scootaloo.thinking.server.dav.service.impl.util

import xyz.scootaloo.thinking.lang.getNullable
import xyz.scootaloo.thinking.lang.transformIfNotNull
import xyz.scootaloo.thinking.lib.HttpHeaderHelper
import xyz.scootaloo.thinking.server.dav.domain.core.Pass
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.util.Convert

/**
 * @author flutterdash@qq.com
 * @since 2022/6/6 13:21
 */
interface DAVCommon : HttpHeaderHelper {

    fun buildPass(ifHeader: String?): Pass? {
        ifHeader ?: return null
        val ifExpr = parseIfHeader(ifHeader).getNullable()
        return ifExpr.transformIfNotNull { Pass(it) }
    }

    fun pathNormalize(path: String): String {
        return PathUtils.normalize(Convert.decodeUriComponent(path))
    }

}