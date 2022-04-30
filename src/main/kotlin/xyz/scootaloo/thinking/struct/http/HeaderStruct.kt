package xyz.scootaloo.thinking.struct.http

import io.vertx.core.json.JsonArray
import xyz.scootaloo.thinking.lang.Type

/**
 * @author flutterdash@qq.com
 * @since 2022/4/28 22:21
 */

/**
 * [reference](https://datatracker.ietf.org/doc/html/rfc4918#section-10.4)
 *
 * If = "If" ":" ( 1*No-tag-list | 1*Tagged-list )
 *
 * No-tag-list = List Tagged-list = Resource-Tag 1*List
 *
 * List = "(" 1*Condition ")"
 * Condition = ["Not"] (State-token | "[" entity-tag "]")
 *             ; entity-tag: see Section 3.11 of [RFC2616]
 *             ; No LWS allowed between "[", entity-tag and "]"
 *
 * State-token = Coded-URL
 *
 * Resource-Tag = "<" Simple-ref ">"
 *                ; Simple-ref: see Section 8.3
 *                ; No LWS allowed in Resource-Tag
 *
 * ----------------------------------------------------------------
 * samples:
 *
 * 10.4.6. If: (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>["I am an ETag"])(["I am another ETag"])
 * 10.4.7. If: (Not <urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2><urn:uuid:58f202ac-22cf-11d1-b12d-002035b29092>)
 * 10.4.8. If: (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>)(Not <DAV:no-lock>)
 * 10.4.9. If: </resource1>(<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>[W/"A weak ETag"]) (["strong ETag"])
 * 10.4.10. If: <http://www.example.com/specs/>(<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>)
 * 10.4.11. If: </specs/rfc2518.doc> (["4217"])
 * 10.4.12. If: </specs/rfc2518.doc> (Not ["4217"])
 *
 * explains:
 *
 * 1. (使用令牌锁定etag为"I am an ETag"的资源)或者(ETag为"I am anther ETag"的资源)
 * 2. (不使用令牌1锁定资源，使用令牌2锁定资源)
 * 3. (使用令牌锁定资源)(取反 -> )
 * 4. 对于资源"/resource1": (使用令牌锁定资源ETag为"A weak ETag"的资源)或者(ETag为"strong ETag"的资源)
 * 5. ...
 */
object IfJsonStruct {
    @Type(JsonArray::class, String::class)
    const val tagged = "tagged"
    const val expr = "expr"
}

/**
 * ```json
 * {
 *     "tagged": string?,
 *     "expr": [Condition]
 * }
 * ```
 */
class IfExpression(
    val expr: List<Condition> = ArrayList(),
    private val tag: String? = null
) {
    fun isTagged(): Boolean = (tag != null)
    fun resTag(): String = tag!!
}

object ConditionJsonStruct {
    const val deny = "deny"
    const val token = "token"
}

/**
 * ```json
 * {
 *     "deny": boolean,
 *     "token": string
 * }
 * ```
 */
class Condition(
    val deny: Boolean,
    val token: String,
    val eTags: Array<ETag>
)

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