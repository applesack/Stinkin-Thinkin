package xyz.scootaloo.thinking.lang

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * @author flutterdash@qq.com
 * @since 2022/4/27 23:08
 */

val INVALID_JSON = JsonObject()
val INVALID_JSON_ARRAY = JsonArray()

operator fun JsonObject.set(key: String, value: Any) {
    put(key, value)
}