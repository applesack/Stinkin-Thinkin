@file:Suppress("unused")

package xyz.scootaloo.thinking.lib

import io.vertx.core.file.FileProps
import io.vertx.core.http.HttpServerRequest
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.struct.http.Range
import java.util.regex.Pattern
import kotlin.math.min

/**
 * @author flutterdash@qq.com
 * @since 2022/5/23 12:52
 */

private const val RANGE = "Range"
private val INVALID_RANGE = Range(-1, -1)
private val RANGE_REG = Pattern.compile("^bytes=(\\d+)-(\\d*)$")

/**
 * 假如解析失败, 返回416状态码
 */
fun HttpHeader.parseRange(request: HttpServerRequest, props: FileProps): Pair<Boolean, Range> {
    val filesize = props.size()
    val range = request.getHeader(RANGE) ?: return true to Range(0, filesize - 1)
    return parseNotNullRange(range, filesize)
}

private fun parseNotNullRange(range: String, filesize: Long): Pair<Boolean, Range> {
    var offset = 0L
    var end = filesize - 1
    return try {
        val matcher = RANGE_REG.matcher(range)
        if (matcher.matches()) {
            var part: String? = matcher.group(1)
            offset = part?.toLong() ?: 0L

            if (offset < 0 || offset > filesize) {
                throw IndexOutOfBoundsException()
            }

            part = matcher.group(2)
            if (part != null && part.isNotEmpty()) {
                end = min(end, part.toLong())
                if (end < offset) {
                    throw IndexOutOfBoundsException()
                }
            }
        }

        true to Range(offset, end)
    } catch (error: Throwable) {
        false to INVALID_RANGE
    }
}

private class LibRangeParseUnitTest : TestDsl {

    @Test
    fun test0(): Unit = HttpHeader.run {
        parseNotNullRange("", 100) check { (valid, _) ->
            valid shouldBe true
        }

        parseNotNullRange("bytes=5-99", 100) check { (valid, range) ->
            valid shouldBe true
            range.offset shouldBe 5
            range.end shouldBe 99
        }

        parseNotNullRange("bytes=5-", 100) check { (valid, range) ->
            valid shouldBe true
            range.offset shouldBe 5
            range.end shouldBe 99
        }

        parseNotNullRange("bytes=5-150", 100) check { (valid, range) ->
            valid shouldBe true
            range.offset shouldBe 5
            range.end shouldBe 99
        }

        parseNotNullRange("", 8) check { (_, range) ->
            range.offset.log()
            range.end.log()
        }

        // 区间等于0, 无效的区间
        parseNotNullRange("bytes=100-100", 100) check { (valid, _) ->
            valid shouldBe false
        }

        // 结束标记小于开始标记
        parseNotNullRange("bytes=100-50", 100) check { (valid, _) ->
            valid shouldBe false
        }
    }

}