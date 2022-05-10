package xyz.scootaloo.thinking.struct.http

import io.vertx.core.json.JsonArray
import xyz.scootaloo.thinking.lang.Type

/**
 * @author flutterdash@qq.com
 * @since 2022/4/28 22:21
 */

object CommonHeader {
    const val timeout = "Timeout"
    const val depth = "Depth"
}

object IfJsonStruct {
    @Type(JsonArray::class, String::class)
    const val tagged = "tagged"
    const val list = "list"
}

/**
 * ```json
 * {
 *     "tagged": string?,
 *     "expr": [
 *         [ConditionItem]
 *     ]
 * }
 * ```
 */
class IfExpression(
    var tagged: String? = null,
    val list: MutableList<List<ConditionItem>> = ArrayList()
)

object ConditionJsonStruct {
    const val not = "not"
    const val token = "token"
    const val etag = "etag"
    const val cid = "id"
}

abstract class ConditionItem(val cid: Int)

class TokenCondition(
    val not: Boolean,
    val token: String
) : ConditionItem(0)

class ETagCondition(
    val not: Boolean,
    val etag: ETag
) : ConditionItem(1)

object ETagJsonStruct {
    const val weak = "weak"
    const val name = "name"
}

/**
 * ```json
 * {
 *     "weak": boolean,
 *     "name": string
 * }
 * ```
 */
class ETag(
    val weak: Boolean,
    val name: String
)