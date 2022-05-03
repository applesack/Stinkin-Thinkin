package xyz.scootaloo.thinking.lib

import io.vertx.core.json.JsonObject
import xyz.scootaloo.thinking.struct.http.ETag
import xyz.scootaloo.thinking.struct.http.IfExpression

/**
 * @author flutterdash@qq.com
 * @since 2022/4/30 23:21
 */
object HttpHeader

interface HttpHeaderHelper {

    fun JsonObject.asIf(): IfExpression = HttpHeader.asIf(this)
    fun JsonObject.asETag(): ETag = HttpHeader.asETag(this)

    fun parseIfHeader(text: String): Pair<Boolean, JsonObject> = HttpHeader.parseIfAsJson(text)
    fun parseETagHeader(text: String): Pair<Boolean, JsonObject> = HttpHeader.parseETagAsJson(text)
    fun parseTimeoutHeader(text: String): Pair<Boolean, Int> = HttpHeader.parseTimeout(text)

}