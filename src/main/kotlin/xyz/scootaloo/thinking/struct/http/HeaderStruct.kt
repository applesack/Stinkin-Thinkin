package xyz.scootaloo.thinking.struct.http

import io.vertx.core.json.JsonArray
import xyz.scootaloo.thinking.lang.Type

/**
 * @author flutterdash@qq.com
 * @since 2022/4/28 22:21
 */

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

abstract class ConditionItem

class TokenCondition(
    val not: Boolean,
    val token: String
) : ConditionItem()

class ETagCondition(
    val not: Boolean,
    val etag: ETag
) : ConditionItem()

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

object DepthJsonStruct {
    const val depth = "depth"
    const val noRoot = "noRoot"
}

class Depth(
    val depth: Int,
    val noRoot: Boolean
)

class Range(
    val offset: Long,
    val end: Long
)

class Timeout(
    val amount: Int,
    val infinite: Boolean
) {
    fun display(): String {
        val timout = "Second-$amount"
        return if (infinite) {
            "$timout,Infinite"
        } else {
            timout
        }
    }
}