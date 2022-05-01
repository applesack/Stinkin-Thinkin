@file:Suppress("unused")

package xyz.scootaloo.thinking.lib

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.struct.http.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 解析If请求标头
 *
 * 将内容解析为json结构, 结构参考
 *
 * @author flutterdash@qq.com
 * @since 2022/4/28 18:35
 */

@Version("1")
@TestOnly
fun HttpHeader.parseIf(text: String): Pair<Boolean, IfExpression> {
    val (valid, json) = parseIfAsJson(text)
    return if (!valid) {
        false to invalidIfExpr
    } else {
        true to asIf(json)
    }
}

fun HttpHeader.asIf(json: JsonObject): IfExpression {
    val expr = IfExpression(json.getString(IfJsonStruct.tagged))
    json.getJsonArray(IfJsonStruct.list).forEach {
        it as JsonArray
        val items = ArrayList<ConditionItem>()
        it.forEach { item ->
            items.add(HttpHeader.asConditionItem(item as JsonObject))
        }
        if (items.isNotEmpty()) {
            expr.list.add(items)
        }
    }
    return expr
}

private fun HttpHeader.asConditionItem(json: JsonObject): ConditionItem {
    val serializeId = json.getInteger(ConditionJsonStruct.cid)
    return if (serializeId == 0) {
        TokenCondition(
            json.getBoolean(ConditionJsonStruct.not),
            json.getString(ConditionJsonStruct.token)
        )
    } else {
        ETagCondition(
            json.getBoolean(ConditionJsonStruct.not),
            asETag(json.getJsonObject(ConditionJsonStruct.etag))
        )
    }
}

private val symbols = arrayOf('<', '(', '[', '>', ')', ']')
private val invalidIfExpr = IfExpression()

@Version("1")
fun HttpHeader.parseIfAsJson(text: String): Pair<Boolean, JsonObject> {
    val stack = Stack<Pair<Char, Int>>()
    val conditions = mutableListOf<List<JsonObject>>()
    val condItems = mutableListOf<JsonObject>()
    val expr = Json.obj {
        this[IfJsonStruct.list] = conditions
    }

    for (pos in text.indices) {
        val ch = text[pos]
        val idx = symbols.indexOf(ch)
        if (idx < 0) {
            continue
        } else if (idx < 3) {
            stack.push(ch to pos)
        } else {
            if (stack.isEmpty())
                return false to INVALID_JSON

            val (begin, beginIdx) = stack.pop()
            if (begin == symbols[idx - 3]) {
                if (begin == '<' && stack.isEmpty()) {
                    expr[IfJsonStruct.tagged] = handleTagged(text, beginIdx, pos)
                    continue
                }
                if (begin == '(') {
                    conditions.add(condItems.copy())
                    condItems.clear()
                    continue
                }
                if (begin == '<') {
                    handleToken(text, beginIdx, pos).ifValid(condItems::add)
                    continue
                }
                if (begin == '[') {
                    handleETag(text, beginIdx, pos).ifValid(condItems::add)
                }
            } else {
                return false to INVALID_JSON
            }
        }
    }

    return true to expr
}

private fun evaluateIfExpr() {

}

private fun handleTagged(text: String, begin: Int, end: Int): String {
    return text.substring(begin + 1, end)
}

private fun handleToken(text: String, begin: Int, end: Int): Pair<Boolean, JsonObject> {
    val rest = text.substring(begin + 1, end)
    if (rest.isEmpty()) {
        return false to INVALID_JSON
    }

    return true to Json.obj {
        this[ConditionJsonStruct.cid] = 0
        this[ConditionJsonStruct.not] = hasNotMark(text, begin)
        this[ConditionJsonStruct.token] = rest
    }
}

private fun handleETag(text: String, begin: Int, end: Int): Pair<Boolean, JsonObject> {
    val rest = text.substring(begin + 1, end)
    val result = HttpHeader.parseETagAsJson(rest)
    if (!result.isValid)
        return result
    return true to Json.obj {
        this[ConditionJsonStruct.cid] = 1
        this[ConditionJsonStruct.not] = hasNotMark(text, begin)
        this[ConditionJsonStruct.etag] = result.second
    }
}

private fun hasNotMark(text: String, pos: Int): Boolean {
    var whitePos = pos - 1
    while (whitePos > 2 && (text[whitePos] == ' ' || text[whitePos] == '\n')) {
        whitePos--
    }
    return (whitePos > 2 && text.substring(whitePos - 2, whitePos + 1) like ConditionJsonStruct.not)
}

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
internal class LibUnitTest0 : TestDsl {
    @Test // 10.4.6
    fun testIfHeader6(): Unit = HttpHeader.run {
        parseIf(
            """
            (<urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>
            ["I am an ETag"])
            (["I am another ETag"])
            """.trimIndent()
        ) check { (valid, expr) ->
            valid shouldBe true
            with(expr) {
                tagged shouldBe null
                list.size shouldBe 2
                list[0] check { conditions ->
                    conditions.size shouldBe 2
                    with(conditions[0] as TokenCondition) {
                        not shouldBe false
                        token shouldBe "urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2"
                    }
                    with(conditions[1] as ETagCondition) {
                        etag.weak shouldBe false
                        etag.name shouldBe "I am an ETag"
                    }
                }
                list[1] check { conditions ->
                    with(conditions[0] as ETagCondition) {
                        etag.weak shouldBe false
                        etag.name shouldBe "I am another ETag"
                    }
                }
            }
        }
    }

    @Test
    fun testIfHeader7(): Unit = HttpHeader.run {
        parseIf(
            """
            (Not <urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2>
            <urn:uuid:58f202ac-22cf-11d1-b12d-002035b29092>)
            """.trimIndent()
        ) check { (valid, expr) ->
            valid shouldBe true
            expr.tagged shouldBe null
            with(expr.list) {
                size shouldBe 1
                this[0] check {
                    it.size shouldBe 2
                    with(it[0] as TokenCondition) {
                        this.not shouldBe true
                        this.token shouldBe "urn:uuid:181d4fae-7d8c-11d0-a765-00a0c91e6bf2"
                    }
                    with(it[1] as TokenCondition) {
                        this.not shouldBe false
                        this.token shouldBe "urn:uuid:58f202ac-22cf-11d1-b12d-002035b29092"
                    }
                }
            }
        }
    }

    @Test
    fun testIfHeader12(): Unit = HttpHeader.run {
        parseIf(
            """
            </specs/rfc2518.doc> (Not ["4217"])
            """.trimIndent()
        ) check { (valid, expr) ->
            valid shouldBe true
            expr.tagged shouldBe "/specs/rfc2518.doc"
            with(expr.list) {
                size shouldBe 1
                this[0] check { conditions ->
                    with(conditions[0] as ETagCondition) {
                        this.not shouldBe true
                        this.etag.weak shouldBe false
                        this.etag.name shouldBe "4217"
                    }
                }
            }
        }
    }
}