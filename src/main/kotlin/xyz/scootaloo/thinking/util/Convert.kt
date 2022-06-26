package xyz.scootaloo.thinking.util

import cn.hutool.core.codec.Base64
import cn.hutool.crypto.digest.MD5
import io.vertx.core.net.impl.URIDecoder
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 13:36
 */
object Convert {

    private val md5 = MD5()
    private val encoder = cn.hutool.core.net.URLEncoder.createDefault()

    fun base64encode(data: String): String {
        return Base64.encode(data)
    }

    fun base64decode(data: String): String {
        return Base64.decodeStr(data)
    }

    fun md5(data: String): String {
        return md5.digestHex(data)
    }

    fun decodeUriComponent(uri: String, plus: Boolean = true): String {
        return URIDecoder.decodeURIComponent(uri, plus)
    }

    fun encodeUriComponent(uri: String): String {
        return encoder.encode(uri, Charsets.UTF_8)
    }

}

private class ConvertUnitTest : TestDsl {

    @Test
    fun test() {
        Convert.encodeUriComponent("/").log()
        Convert.encodeUriComponent("/你好世界").log()
        Convert.encodeUriComponent("/你好世界/你好世界hello.txt").log()
    }

}