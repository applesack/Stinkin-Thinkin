package xyz.scootaloo.thinking.lib

import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.Version
import java.util.*

/**
 * 解析If请求标头
 *
 * 将内容解析为json结构, 结构参考
 *
 * @author flutterdash@qq.com
 * @since 2022/4/28 18:35
 */
private val symbols = arrayOf('<', '(', '[', '>', ')', ']')

@Version("1")
fun parseIfHeader(text: String): JsonObject {
    val map = hashMapOf<String, Any>()
    val stack = Stack<Pair<Char, Int>>()

    for (ch in text) {
        val idx = symbols.indexOf(ch)
        if (idx < 0) {
            continue
        } else if (idx < 3) {
            stack.push(ch to idx)
        } else {
            val top = stack.peek()
            if (symbols[idx - 3] == top.first) {
                handle(map, text, top.second, idx)
            } else {
                return shortCut(map)
            }
        }
    }

    return shortCut(map)
}

private fun shortCut(map: HashMap<String, Any>): JsonObject {
    TODO()
}

private fun evaluate() {
}

private fun handle(map: Map<String, Any>, text: String, start: Int, end: Int) {
    // 否定
    if (end - start <= 1)
        return
}

internal class LibUnitTest0 {
    @Test
    fun testIfHeader() {

    }
}