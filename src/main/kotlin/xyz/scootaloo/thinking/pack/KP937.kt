package xyz.scootaloo.thinking.pack

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl

/**
 * @author flutterdash@qq.com
 * @since 2022/5/3 0:19
 */
class KP937 : TestDsl {

    fun reorderLogFiles(logs: Array<String>): Array<String> {
        val letterLogs = ArrayList<Pair<String, String>>()
        val numberLogs = ArrayList<Pair<String, String>>()
        logs.forEach {
            val white = it.indexOf(' ')
            val identifier = it.substring(0, white)
            val content = it.substring(white + 1, it.length)
            for (ch in content) {
                if (ch == ' ') {
                    continue
                }
                if (ch !in '0'..'9') {
                    letterLogs.add(identifier to content)
                    return@forEach
                }
            }

            numberLogs.add(identifier to content)
        }

        letterLogs.sortWith { a, b ->
            val diff = a.second.compareTo(b.second)
            if (diff == 0) {
                a.first.compareTo(b.first)
            } else {
                diff
            }
        }

        val res = Array(logs.size) {
            if (it < letterLogs.size) {
                letterLogs[it].run { "$first $second" }
            } else {
                numberLogs[it - letterLogs.size].run { "$first $second" }
            }
        }

        return res
    }

    @Test
    fun test() {
        val input = arrayOf(
            "dig1 8 1 5 1",
            "let1 art can",
            "dig2 3 6",
            "let2 own kit dig",
            "let3 art zero"
        )
        reorderLogFiles(input).log()
    }

    @Test
    fun test2() {
        val input = arrayOf(
            "a1 9 2 3 1", "g1 act car", "zo4 4 7", "ab1 off key dog", "a8 act zoo"
        )
        reorderLogFiles(input).log()
    }

}