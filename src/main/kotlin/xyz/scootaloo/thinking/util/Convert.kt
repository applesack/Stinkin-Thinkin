package xyz.scootaloo.thinking.util

import cn.hutool.core.codec.Base64
import cn.hutool.crypto.digest.MD5

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 13:36
 */
object Convert {

    private val md5 = MD5()

    fun base64encode(data: String): String {
        return Base64.encode(data)
    }

    fun base64decode(data: String): String {
        return Base64.decodeStr(data)
    }

    fun md5(data: String): String {
        return md5.digestHex(data)
    }

}