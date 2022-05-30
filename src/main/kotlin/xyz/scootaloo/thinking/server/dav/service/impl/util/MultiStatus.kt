package xyz.scootaloo.thinking.server.dav.service.impl.util

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.set

/**
 * @author flutterdash@qq.com
 * @since 2022/5/25 10:48
 */
object MultiStatus {

    private const val href = "Href"
    private const val status = "Status"

    fun build(
        receiver: JsonObject, subject: String, code: Int,
        httpVersion: String = "HTTP/1.1",
    ) {
        val details = HttpResponseStatus.valueOf(code)
        receiver[href] = subject
        receiver[status] = "$httpVersion $code ${details.reasonPhrase()}"
    }

    fun statusOf(code: Int): String {
        return HttpResponseStatus.valueOf(code).reasonPhrase()
    }

}

private class MultiStatusUnitTest : TestDsl {

    @Test
    fun test() {
        val json = JsonObject()
        MultiStatus.build(json, "/a.html", 404)
        json.log()
    }

    @Test
    fun test1() {
        val json = JsonObject()
        MultiStatus.build(json, "/test.c", 424)
        json.log()
    }

    @Test
    fun test2() {
        val json = JsonObject()
        MultiStatus.build(json, "/test", 0)
        json.log()
    }

}