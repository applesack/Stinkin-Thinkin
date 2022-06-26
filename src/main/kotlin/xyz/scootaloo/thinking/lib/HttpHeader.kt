package xyz.scootaloo.thinking.lib

import io.vertx.core.file.FileProps
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.impl.MimeMapping
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
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
        return (if (filename.lastIndexOf('.') > 0) {
            MimeMapping.getMimeTypeForFilename(filename)
        } else {
            MimeMapping.getMimeTypeForExtension(filename)
        }) ?: MimeMapping.getMimeTypeForExtension("bin")!!
    }

}

private class HttpHeaderUnitTest : HttpHeaderHelper, TestDsl {

    @Test
    fun test() {
        contentTypeOf("xml").log()
    }

}