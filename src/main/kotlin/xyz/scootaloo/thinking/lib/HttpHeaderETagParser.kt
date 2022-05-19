@file:Suppress("unused")

package xyz.scootaloo.thinking.lib

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.struct.http.ETag
import xyz.scootaloo.thinking.struct.http.ETagJsonStruct

/**
 * @author flutterdash@qq.com
 * @since 2022/4/30 21:50
 */

private const val weakMark = "\\W"
private val invalidETag = ETag(false, "")

@TestOnly
fun HttpHeader.parseETag(text: String): Pair<Boolean, ETag> {
    val (valid, json) = parseETagAsJson(text)
    return if (!valid) {
        false to invalidETag
    } else {
        true to HttpHeader.asETag(json)
    }
}

@Version("1")
fun HttpHeader.parseETagAsJson(text: String): Pair<Boolean, JsonObject> {
    var rest = text.trim()
    val weak = rest.startsWith(weakMark, true)
    rest = if (weak) rest.substring(2) else rest
    if (rest.startsWith('\"')) {
        rest = rest.trim('\"')
    }
    return if (rest.isEmpty()) {
        false to INVALID_JSON
    } else {
        true to Json.obj {
            this[ETagJsonStruct.weak] = weak
            this[ETagJsonStruct.name] = rest
        }
    }
}

fun HttpHeader.asETag(json: JsonObject): ETag {
    return ETag(
        json.getBoolean(ETagJsonStruct.weak),
        json.getString(ETagJsonStruct.name)
    )
}

class LibUnitTest1 : TestDsl {

    @Test
    fun testETag() = HttpHeader.run {
        val normalCase: (Pair<Boolean, ETag>) -> Unit = { (valid, etag) ->
            valid shouldBe true
            etag.run {
                this.weak shouldBe true
                this.name shouldBe "I am a Tag"
            }
        }

        parseETag(""" \W"I am a Tag" """) check normalCase

        parseETag(""" \w"I am a Tag" """) check normalCase

        parseETag("""\wI am a Tag""") check normalCase

        parseETag("""\wI am a Tag  """) check normalCase

        parseETag(""" "I am a Tag """) check { (valid, etag) ->
            valid shouldBe true
            etag.run {
                this.weak shouldBe false
                this.name shouldBe "I am a Tag"
            }
        }

        parseETag("""""") check { (valid, _) ->
            valid shouldBe false
        }
    }

}