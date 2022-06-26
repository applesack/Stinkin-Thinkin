package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.MultiMap
import io.vertx.core.file.FileProps
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.lang.ifNull
import xyz.scootaloo.thinking.lib.HttpHeaderHelper
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.StaticResourcesService
import xyz.scootaloo.thinking.server.dav.service.impl.util.Helper
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.struct.http.Range
import xyz.scootaloo.thinking.util.Convert
import xyz.scootaloo.thinking.util.DateHelper
import java.nio.charset.Charset

/**
 * @author flutterdash@qq.com
 * @since 2022/5/23 12:11
 */
object StaticResourcesImpl : SingletonVertxService(), StaticResourcesService {
    private val log by lazy { getLogger("s-resources") }
    override val context = WebDAVContext.file

    override suspend fun handle(ctx: RoutingContext) {
        val filename = solveFilename(ctx.pathParam("*"))
        Resources.sendFile(ctx, filename)
    }

    private fun solveFilename(file: String): String {
        return PathUtils.normalize(Convert.decodeUriComponent(file))
    }

    private object Resources : HttpHeaderHelper {
        suspend fun sendFile(ctx: RoutingContext, filename: String) {
            val fullFilePath = Helper.fullPath(filename)
            try {
                if (!fs.exists(fullFilePath).await()) {
                    ctx.fail(Status.notFound)
                }

                val fileProps = fs.props(fullFilePath).await()
                if (fileProps.isDirectory) { // 暂时不处理发送目录的情况
                    ctx.fail(Status.internalError)
                    return
                }

                sendRangeFile(ctx, fullFilePath, fileProps)
            } catch (error: Throwable) {
                log.error("en error when send file: $filename", error)
                ctx.fail(Status.internalError)
            }
        }

        private fun sendRangeFile(
            ctx: RoutingContext, fullFilePath: String, props: FileProps,
        ) {
            val request = ctx.request()
            val response = ctx.response()

            if (response.closed()) {
                return
            }

            val (valid, range) = parseRangeHeader(request, props)
            if (!valid) {
                response.putHeader(HttpHeaders.CONTENT_RANGE, "bytes */${props.size()}")
                ctx.fail(Status.rangeNotSatisfiable)
                return
            }

            attachFileInfoToHeader(response, fullFilePath, range, props)

            if (request.method() == HttpMethod.HEAD) {
                response.end()
                return
            }

            doSendRangeFile(ctx, fullFilePath, props, range.offset, range.end)
        }

        private val defaultContentEncoding = Charset.defaultCharset().name() // 本地默认编码
        private const val RANGE = "Range"

        private fun doSendRangeFile(
            ctx: RoutingContext, fullFilePath: String,
            props: FileProps, offset: Long, end: Long,
        ) {
            val request = ctx.request()
            val response = ctx.response()
            val headers = response.headers()

            // 写入文件的range信息
            if (request.getHeader(RANGE) != null || request.getHeader(RANGE.lowercase()) != null) {
                headers[HttpHeaders.CONTENT_RANGE] = "bytes $offset-$end/${props.size()}"
                response.statusCode = Status.partialContent
            } else {
                response.statusCode = Status.ok
            }

            val readLength = end + 1 - offset
            response.sendFile(fullFilePath, offset, readLength) { done ->
                if (done.failed()) {
                    ctx.fail(Status.internalError)
                }
            }
        }

        private const val TEXT = "text"

        private fun attachFileInfoToHeader(
            response: HttpServerResponse, filename: String,
            range: Range, props: FileProps,
        ) {
            val headers = response.headers()

            // 告知客户端服务器可以处理的分段格式
            // 向响应体写入请求体的长度
            // 假如是文本文件, 则在响应中写入编码信息

            headers[HttpHeaders.ACCEPT_RANGES] = "bytes"
            headers[HttpHeaders.CONTENT_LENGTH] = (range.end + 1 - range.offset).toString()

            val contentType = contentTypeOf(filename)
            if (contentType.startsWith(TEXT)) {
                headers[HttpHeaders.CONTENT_TYPE] = "$contentType;charset=$defaultContentEncoding"
            } else {
                headers[HttpHeaders.CONTENT_TYPE] = contentType
            }

            // 告知客户端缓存本次响应

            writeCacheHeaders(response, props)
        }

        private const val DATE = "date"
        private const val maxAgeSeconds = 86400 // 缓存一天
        private const val cacheControl = "public, immutable, max-age=$maxAgeSeconds"

        private fun writeCacheHeaders(response: HttpServerResponse, props: FileProps) {
            val headers = response.headers()
            headers.putIfAbsent(HttpHeaders.CACHE_CONTROL, cacheControl)
            headers.putIfAbsent(
                HttpHeaders.LAST_MODIFIED, DateHelper.formatRFC1123(props.lastModifiedTime())
            )
            headers[DATE] = DateHelper.formatRFC1123()
        }

        private fun MultiMap.putIfAbsent(key: CharSequence, value: String) {
            this[key].ifNull {
                this[key] = value
            }
        }
    }

    private object Status {
        const val ok = 200
        const val partialContent = 206
        const val internalError = 500
        const val notFound = 404
        const val rangeNotSatisfiable = 416
    }

}