package xyz.scootaloo.thinking.lib

import io.vertx.core.json.JsonObject
import xyz.scootaloo.thinking.struct.http.Depth
import xyz.scootaloo.thinking.struct.http.ETag
import xyz.scootaloo.thinking.struct.http.IfExpression

/**
 * @author flutterdash@qq.com
 * @since 2022/4/30 23:21
 */
object HttpHeader

interface HttpHeaderHelper {

    fun parseIfHeader(text: String): Pair<Boolean, IfExpression> = HttpHeader.parseIf(text)
    fun parseETagHeader(text: String): Pair<Boolean, ETag> = HttpHeader.parseETag(text)
    fun parseTimeoutHeader(text: String): Pair<Boolean, Int> = HttpHeader.parseTimeout(text)
    fun parseDepthHeader(text: String): Depth = HttpHeader.parseDepth(text)

}