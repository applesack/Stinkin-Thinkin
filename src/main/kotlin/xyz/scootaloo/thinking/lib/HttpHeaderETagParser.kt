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

@Version("1")
@TestOnly
fun parseETagHeader(text: String): Pair<Boolean, ETag> {
    val (valid, json) = parseETagHeaderAsJson(text)
    return if (!valid) {
        false to invalidETag
    } else {
        true to etagFromJson(json)
    }
}

@Version("1")
fun parseETagHeaderAsJson(text: String): Pair<Boolean, JsonObject> {
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

fun etagFromJson(json: JsonObject): ETag {
    return ETag(
        json.getBoolean(ETagJsonStruct.weak),
        json.getString(ETagJsonStruct.name)
    )
}

class LibUnitTest1 : TestDsl {

    @Test
    fun testETag() {
        val normalCase: (Pair<Boolean, ETag>) -> Unit = { (valid, etag) ->
            valid shouldBe true
            etag.run {
                this.weak shouldBe true
                this.name shouldBe "I am a Tag"
            }
        }

        parseETagHeader(""" \W"I am a Tag" """) check normalCase

        parseETagHeader(""" \w"I am a Tag" """) check normalCase

        parseETagHeader("""\wI am a Tag""") check normalCase

        parseETagHeader("""\wI am a Tag  """) check normalCase

        parseETagHeader(""" "I am a Tag """) check { (valid, etag) ->
            valid shouldBe true
            etag.run {
                this.weak shouldBe false
                this.name shouldBe "I am a Tag"
            }
        }

        parseETagHeader("""""") check { (valid, _) ->
            valid shouldBe false
        }
    }

}