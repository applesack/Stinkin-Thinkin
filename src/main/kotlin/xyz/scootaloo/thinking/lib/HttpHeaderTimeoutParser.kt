package xyz.scootaloo.thinking.lib

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.like

/**
 * @author flutterdash@qq.com
 * @since 2022/5/2 13:36
 */

fun HttpHeader.parseTimeout(text: String): Pair<Boolean, Int> {
    for (segment in text.split(',')) {
        val rest = segment.trim()
        if (rest like "Infinite") {
            return true to -1
        } else if (rest.startsWith("Second-") && rest.length > 7) {
            return safeParseTimeout(rest)
        }
    }

    return false to 0
}

private fun safeParseTimeout(rest: String): Pair<Boolean, Int> {
    return try {
        true to rest.substring(7).toInt()
    } catch (ignore: Throwable) {
        false to 0
    }
}

/**
 * TimeOut = "Timeout" ":" 1#TimeType
 * TimeType = ("Second-" DAVTimeOutVal | "Infinite")
 *            ; No LWS allowed within TimeType
 * DAVTimeOutVal = 1*DIGIT
 */
class LibUnitTest2 : TestDsl {

    @Test
    fun test(): Unit = HttpHeader.run {
        parseTimeout("Second-") check { (valid, _) ->
            valid shouldBe false
        }

        parseTimeout("Second-0") check { (valid, amount) ->
            valid shouldBe true
            amount shouldBe 0
        }

        parseTimeout(" Second-300 ") check { (valid, amount) ->
            valid shouldBe true
            amount shouldBe 300
        }

        parseTimeout("Infinite, Second-300 ") check { (valid, amount) ->
            valid shouldBe true
            amount shouldBe -1
        }
    }

}