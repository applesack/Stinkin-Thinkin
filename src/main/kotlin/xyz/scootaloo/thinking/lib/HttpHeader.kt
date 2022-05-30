package xyz.scootaloo.thinking.lib

import io.vertx.core.file.FileProps
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.impl.MimeMapping
import xyz.scootaloo.thinking.struct.http.*

/**
 * @author flutterdash@qq.com
 * @since 2022/4/30 23:21
 */
object HttpHeader

private typealias Request = HttpServerRequest

interface HttpHeaderHelper {

    fun parseIfHeader(text: String): Pair<Boolean, IfExpression> = HttpHeader.parseIf(text)
    fun parseETagHeader(text: String): Pair<Boolean, ETag> = HttpHeader.parseETag(text)
    fun parseTimeoutHeader(text: String): Pair<Boolean, Timeout> = HttpHeader.parseTimeout(text)
    fun parseDepthHeader(text: String): Depth = HttpHeader.parseDepth(text)
    fun parseRangeHeader(request: Request, props: FileProps): Pair<Boolean, Range> =
        HttpHeader.parseRange(request, props)

    fun contentTypeOf(filename: String): String {
        return MimeMapping.getMimeTypeForFilename(filename) ?: MimeMapping.getMimeTypeForExtension("bin")!!
    }

}