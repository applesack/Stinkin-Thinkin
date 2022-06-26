@file:Suppress("unused")

package xyz.scootaloo.thinking.lib

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.TestOnly
import xyz.scootaloo.thinking.lang.Version
import xyz.scootaloo.thinking.lang.set
import xyz.scootaloo.thinking.struct.http.Depth
import xyz.scootaloo.thinking.struct.http.DepthJsonStruct

/**
 * Depth = "Depth" ":" ("0" | "1" | "1,noroot" | "infinity" | "infinity,noroot")
 *
 * @author flutterdash@qq.com
 * @since 2022/5/16 12:43
 */

@TestOnly
fun HttpHeader.parseDepth(text: String): Depth {
    val result = parseDepthAsJson(text)
    return Depth(
        result.getInteger(DepthJsonStruct.depth),
        result.getBoolean(DepthJsonStruct.noRoot)
    )
}

@Version("1")
fun HttpHeader.parseDepthAsJson(text: String): JsonObject {
    var noRoot = false
    var depth = 0

    for (ch in text) {
        when (ch) {
            '0' -> depth = 0
            '1' -> depth = 1
            'y' -> depth = -1
            'r' -> noRoot = true
        }
    }

    return Json.obj {
        this[DepthJsonStruct.depth] = depth
        this[DepthJsonStruct.noRoot] = noRoot
    }
}

class LibUnitTest3 : TestDsl {

    @Test
    fun test0(): Unit = HttpHeader.run {
        parseDepth("0") check {
            it.noRoot shouldBe false
            it.depth shouldBe 0
        }
        parseDepth("1") check {
            it.noRoot shouldBe false
            it.depth shouldBe 1
        }
        parseDepth("1, noroot") check {
            it.noRoot shouldBe true
            it.depth shouldBe 1
        }
        parseDepth("infinity,noroot") check {
            it.noRoot shouldBe true
            it.depth shouldBe -1
        }
    }

}