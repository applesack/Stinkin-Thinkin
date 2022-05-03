package xyz.scootaloo.thinking.lang

import io.vertx.core.Future
import io.vertx.core.MultiMap
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.eventbus.deliveryOptionsOf

/**
 * @author flutterdash@qq.com
 * @since 2022/4/25 13:34
 */

typealias GenericMessageFormat = JsonObject

val Message<*>.headers: MultiMap get() = headers()

fun EventBus.callService(address: String, data: GenericMessageFormat): Future<Message<JsonObject>> {
    return request(address, data, deliveryOptionsOf(localOnly = true))
}

object InternalMessageConstant {
    const val header = "eb_header"
    const val messageType = "eb_message_type"
}

object InternalMessageType {
    const val emptyContent = 0
    const val xmlContent = 3
}