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
import xyz.scootaloo.thinking.server.dav.util.ContentType
import xyz.scootaloo.thinking.server.dav.util.JsonToXml

/**
 * @author flutterdash@qq.com
 * @since 2022/4/25 13:34
 */

typealias GenericMessageFormat = JsonObject

fun EventBus.callService(address: String, data: GenericMessageFormat): Future<Message<JsonObject>> {
    return request(address, data, deliveryOptionsOf(localOnly = true))
}

interface EventbusMessageHelper {

    suspend fun RoutingContext.smartReply(ebMessage: JsonObject) {
        val state = ebMessage.getInteger(BusJsonConstant.state)
        if (state != null && state != 0) {
            response().statusCode = state
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
            else -> {
                throw UnsupportedOperationException("content-type '$contType' not support")
            }
        }
    }

    fun buildXmlMessage(
        shell: String, state: Int = 0, lazy: (StateHolder<Int, Any>) -> Unit,
    ): SmartJsonMessage {
        return SmartJsonMessage(state, MessageType.XML.code, shell, null, lazy)
    }

    fun buildHtmlMessage(lazy: (StateHolder<Int, Any>) -> Unit): SmartJsonMessage {
        return SmartJsonMessage(0, MessageType.HTML.code, 0, null, lazy)
    }

    fun RoutingContext.replyWithXml(xml: String) {
        response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.xml())
        end(xml)
    }

    fun RoutingContext.replyWithHtml(html: String) {
        response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.html())
        end(html)
    }

    enum class MessageType(val code: Int) {
        XML(0),
        REST(1),
        HTML(3)
    }

    class SmartJsonMessage(
        override var state: Int,
        private val guide: Int,
        private val extra: Any? = null,
        private var body: Any? = null,
        private val lazy: (StateHolder<Int, Any>) -> Unit,
    ) : StateHolder<Int, Any>() {

        override var data: Any?
            get() = body
            set(value) {
                body = value
            }

        fun reply(message: Message<*>) {
            lazy(this)
            val content = Json.obj {
                this[BusJsonConstant.guide] = guide
                this[BusJsonConstant.state] = state
                this[BusJsonConstant.extra] = extra
                this[BusJsonConstant.body] = body
            }
            message.reply(content)
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
    }

}