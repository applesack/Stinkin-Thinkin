@file:Suppress("unused")

package xyz.scootaloo.thinking.lib

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.ifNotNull
import xyz.scootaloo.thinking.lang.like
import xyz.scootaloo.thinking.struct.http.Timeout

/**
 * @author flutterdash@qq.com
 * @since 2022/5/2 13:36
 */

private val INVALID_TIMEOUT = Timeout(-1, false)

fun HttpHeader.parseTimeout(text: String): Pair<Boolean, Timeout> {
    var infinite = false
    var amount = -1
    for (segment in text.split(',')) {
        val rest = segment.trim()
        if (rest like "Infinite") {
            infinite = true
        } else if (rest.startsWith("Second-") && rest.length > 7) {
            safeParseTimeout(rest).ifNotNull {
                amount = it
            }
        }
    }

    if (amount < 0)
        return false to INVALID_TIMEOUT
    return true to Timeout(amount, infinite)
}

private fun safeParseTimeout(rest: String): Int? {
    return try {
        rest.substring(7).toInt()
    } catch (ignore: Throwable) {
        null
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

        parseTimeout("Second-0") check { (valid, timeout) ->
            valid shouldBe true
            timeout.amount shouldBe 0
        }

        parseTimeout(" Second-300 ") check { (valid, timeout) ->
            valid shouldBe true
            timeout.amount shouldBe 300
        }

        parseTimeout("Infinite, Second-300 ") check { (valid, timeout) ->
            valid shouldBe true
            timeout.amount shouldBe 300
            timeout.infinite shouldBe true
        }
    }

}