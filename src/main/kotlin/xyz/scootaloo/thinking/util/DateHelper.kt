package xyz.scootaloo.thinking.util

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.currentTimeMillis
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author flutterdash@qq.com
 * @since 2022/5/17 19:50
 */
object DateHelper {

    private val rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME
    private val zoneId = ZoneId.systemDefault()

    fun formatRFC1123(date: Long): String {
        val instant = Date(date).toInstant()
        val zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId)
        return rfc1123.format(zonedDateTime)
    }

    fun formatDateTimeTZ(date: Long): String {
        val instant = Date(date).toInstant()
        return LocalDateTime.ofInstant(instant, zoneId).toString()
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