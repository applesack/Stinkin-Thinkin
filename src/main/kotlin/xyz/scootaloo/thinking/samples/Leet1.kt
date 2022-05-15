package xyz.scootaloo.thinking.samples

import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author flutterdash@qq.com
 * @since 2022/5/12 11:37
 */
class Leet1 {

    fun solve(content: String): String {
        val result = search(collect(content))
        return result.map { it.second }
            .sortedBy { it.length }
            .take(1)
            .first()
    }

    private fun search(map: Map<String, String>): List<Pair<String, String>> {
        val candidates = LinkedList<Pair<String, String>>()
        for ((key, value) in map) {
            if (match(key, value)) {
                candidates.add(key to value)
            }
        }
        return candidates
    }

    private fun match(key: String, value: String): Boolean {
        if (multiple(key.length, value.length)) {
            if (value.contains('j') || value.contains('b')) {
                return true
            }
        }
        return false
    }

    private fun multiple(a: Int, b: Int): Boolean {
        return (a.toDouble() / b.toDouble()) == (a / b).toDouble()
    }

    private fun collect(content: String): TreeMap<String, String> {
        val notNullItems = content.replace('，', ',').split(',').filter {
            it.isNotBlank()
        }
        var pos = 0
        val map = TreeMap<String, String>()
        while (pos < notNullItems.size) {
            val key = notNullItems[pos++]
            if (pos < notNullItems.size) {
                val value = notNullItems[pos++]
                map[key] = value
            } else {
                break
            }
        }

        return map
    }

    @Test
    fun test() {
        val str =
            "a，likms，klkd，lkjfd_skejf，a，b，bc，c，ldif，oij，" +
                    "lke52，pekrjw，ksdf，lsdf，ba，es，ls，kpoej，jlld" +
                    "，woerljawkllw，p.kuewxhkl，outbkhdh，bi，lr，bsdf，kjiesak"
        solve(str)
    }

}