package xyz.scootaloo.thinking.lang

import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.eventbus.deliveryOptionsOf
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.awaitBlocking
import xyz.scootaloo.thinking.lib.HttpHeaderHelper
import xyz.scootaloo.thinking.server.dav.util.JsonToXml

/**
 * @author flutterdash@qq.com
 * @since 2022/4/25 13:34
 */

typealias GenericMessageFormat = JsonObject

fun EventBus.callService(address: String, data: GenericMessageFormat): Future<Message<JsonObject>> {
    return request(address, data, deliveryOptionsOf(localOnly = true))
}

interface EventbusMessageHelper : HttpHeaderHelper {

    suspend fun RoutingContext.smartReply(ebMessage: JsonObject) {
        val state = ebMessage.getInteger(BusJsonConstant.state)
        if (state != null && state != 0) {
            response().statusCode = state
        }

        ebMessage.getJsonObject(BusJsonConstant.header).ifNotNull { headers ->
            val response = response()
            for ((key, value) in headers) {
                response.putHeader(key, value.toString())
            }
        }

        when (val contType = ebMessage.getInteger(BusJsonConstant.guide)) {
            MessageType.XML.code -> {
                val xmlJson = ebMessage.getJsonObject(BusJsonConstant.body)
                val xmlShell = ebMessage.getString(BusJsonConstant.extra)
                val xml = awaitBlocking { JsonToXml.convert(xmlJson, xmlShell) }
                replyWithXml(xml)
            }
            MessageType.REST.code -> {
                throw UnsupportedOperationException("content-type restful not support")
            }
            MessageType.HTML.code -> {
                val html = ebMessage.getString(BusJsonConstant.body)
                replyWithHtml(html)
            }
            MessageType.RAW.code -> {
                val content = ebMessage.getString(BusJsonConstant.body)
                reply(content)
            }
            else -> {
                throw UnsupportedOperationException("content-type '$contType' not support")
            }
        }
    }

    fun buildXmlMessage(
        shell: String, state: Int = 0, lazy: (MessageEdit) -> Unit,
    ): SmartJsonMessage {
        val message = MessageEdit()
        message.extra = shell
        return SmartJsonMessage(MessageType.XML.code, message, lazy)
    }

    fun buildHtmlMessage(lazy: (MessageEdit) -> Unit): SmartJsonMessage {
        val message = MessageEdit()
        return SmartJsonMessage(MessageType.HTML.code, message, lazy)
    }

    fun buildRawMessage(lazy: (MessageEdit) -> Unit): SmartJsonMessage {
        val message = MessageEdit()
        return SmartJsonMessage(MessageType.RAW.code, message, lazy)
    }

    fun RoutingContext.replyWithXml(xml: String) {
        response().putHeader(HttpHeaders.CONTENT_TYPE, contentTypeOf("xml"))
        end(xml)
    }

    fun RoutingContext.reply(msg: String?) {
        if (msg != null) {
            end(msg)
        } else {
            end()
        }
    }

    fun RoutingContext.replyWithHtml(html: String) {
        response().putHeader(HttpHeaders.CONTENT_TYPE, contentTypeOf("html"))
        end(html)
    }

    enum class MessageType(val code: Int) {
        XML(0),
        REST(1),
        HTML(3),
        RAW(4)
    }

    class SmartJsonMessage(
        private val guide: Int = 0,
        private var message: MessageEdit = MessageEdit(),
        private val lazy: (MessageEdit) -> Unit,
    ) {
        fun reply(request: Message<*>) {
            lazy(message)
            val content = Json.obj {
                this[BusJsonConstant.guide] = guide
                this[BusJsonConstant.state] = message.state
                this[BusJsonConstant.extra] = message.extra
                this[BusJsonConstant.body] = message.body
                if (message.header.isNotEmpty()) {
                    this[BusJsonConstant.header] = message.header
                }
            }
            request.reply(content)
        }
    }

    class MessageEdit(
        var extra: Any? = null,
        var state: Int = 0,
        var body: Any? = null,
        var header: MutableMap<String, String> = HashMap(),
    ) {
        fun putHeader(key: String, value: String) {
            header[key] = value
        }
    }

    /**
     * ```json
     * {
     *     "_eb_guide": 1,
     *     "_state": 404,
     *     "_body": {
     *         ... ...
     *     }
     * }
     * ```
     */
    private object BusJsonConstant {
        const val guide = "_eb_guide"
        const val state = "_state"
        const val extra = "_extra"
        const val body = "_body"
        const val header = "_header"
    }

}