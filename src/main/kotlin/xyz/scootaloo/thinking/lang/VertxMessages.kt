package xyz.scootaloo.thinking.lang

import io.vertx.core.MultiMap
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.core.http.impl.headers.HeadersMultiMap
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.deliveryOptionsOf

/**
 * @author flutterdash@qq.com
 * @since 2022/4/25 13:34
 */

val Message<*>.headers: MultiMap get() = headers()

inline operator fun Message<*>.invoke(init: MessageBuilder.() -> Unit) {
    MessageBuilder(this).init()
}

class MessageBuilder(private val message: Message<*>) {
    val headers: MultiMap = HeadersMultiMap()

    fun replyWithHttp204() {

    }

    fun reply() {
        message.reply(null, deliveryOptions())
    }

    fun reply(body: JsonObject) {
        message.reply(body, deliveryOptions())
    }

    private fun deliveryOptions(): DeliveryOptions {
        return deliveryOptionsOf().apply {
            if (!this@MessageBuilder.headers.isEmpty)
                this.headers = this@MessageBuilder.headers
            isLocalOnly = true
        }
    }
}