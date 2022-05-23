package xyz.scootaloo.thinking.util

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.currentTimeMillis
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author flutterdash@qq.com
 * @since 2022/5/17 19:50
 */
object DateHelper {

    private val rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME
    private val ZONE_GMT = ZoneId.of("GMT")

    fun formatRFC1123(date: Long = currentTimeMillis()): String {
        return rfc1123.format(Instant.ofEpochMilli(date).atZone(ZONE_GMT))
    }

    fun formatDateTimeTZ(date: Long): String {
        val instant = Date(date).toInstant()
        return LocalDateTime.ofInstant(instant, ZONE_GMT).toString()
    }

}

private class DateHelperUnitTest : TestDsl {

    @Test
    fun test() {
        val date = currentTimeMillis()
        DateHelper.formatRFC1123(date).log()
        DateHelper.formatDateTimeTZ(date).log()
    }

}