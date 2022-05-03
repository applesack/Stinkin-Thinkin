package xyz.scootaloo.thinking.pack

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import java.util.*

/**
 * @author flutterdash@qq.com
 * @since 2022/5/2 10:37
 */
class KP591 : TestDsl {

    fun isValid(code: String): Boolean {
        val stack = Stack<String>()
        var idx = 0
        var deep = 0
        while (idx < code.length) {
            val ch = code[idx]
            if (ch == '<') {
                val (end, isTag, tagName) = readLabel(code, idx)
                idx = end
                if (!isTag) {
                    if (deep <= 0)
                        return false
                    continue
                }
                if (tagName.startsWith('/')) {
                    if (tagName.length < 2 || stack.isEmpty()) return false
                    val realTag = tagName.substring(1)
                    if (stack.peek() == realTag) {
                        if (deep > 1) {
                            deep--
                        } else if (deep == 1) {
                            deep = -1
                        }
                        stack.pop()
                    } else {
                        return false
                    }
                } else {
                    if (isTagNameValid(tagName)) {
                        if (deep >= 0) {
                            deep++
                        } else {
                            return false
                        }
                        stack.push(tagName)
                    } else {
                        return false
                    }
                }
                continue
            } else {
                if (deep <= 0)
                    return false
            }
            idx++
        }

        return stack.isEmpty()
    }

    // endIdx, isTag, tagName
    private fun readLabel(code: String, start: Int): Triple<Int, Boolean, String> {
        var expect = ">"
        var pos = start + 1
        // </abc>
        // <![CDATA[><]]>
        while (pos < code.length) {
            if (pos - start == 9 && code.subSequence(start, pos) == "<![CDATA[") {
                expect = "]]>"
                pos++
                continue
            }

            if (code.substring(pos - expect.length + 1, pos + 1) == expect) {
                return Triple(
                    pos + 1, expect.length == 1,
                    if (expect.length == 1) code.substring(start + 1, pos) else ""
                )
            }
            pos++
        }
        return Triple(code.length, false, "")
    }

    // 3
    private fun isTagNameValid(name: String): Boolean {
        if (name.length !in 1..9) return false
        return name.all { it in 'A'..'Z' }
    }

    @Test
    fun testLabelParse() {
        readLabel("</code>", 0) check { (end, isTag, name) ->
            end shouldBe 7
            isTag shouldBe true
            name shouldBe "/code"
        }
        readLabel("<![CDATA[abc]]>", 0) check { (end, isTag, _) ->
            end shouldBe 15
            isTag shouldBe false
        }
        readLabel("<![CDATA[wahaha]]]><![CDATA[]> wahaha]]>", 0) check { (end, isTag, _) ->
            end shouldBe 19
            isTag shouldBe false
        }
    }

    @Test
    fun test() {
        isValid("<DIV>This is the first line <![CDATA[<div>]]></DIV>") shouldBe true
        isValid("<DIV>>>  ![cdata[]] <![CDATA[<div>]>]]>]]>>]</DIV>") shouldBe true

        isValid("<A>  <B> </A>   </B>") shouldBe false
        isValid("<DIV>  div tag is not closed  <DIV>") shouldBe false
        isValid("<DIV>  unmatched <  </DIV>") shouldBe false
        isValid("<DIV> closed tags with invalid tag name  <b>123</b> </DIV>") shouldBe false
        isValid(
            "<DIV> unmatched tags with invalid tag name  </1234567890> and <CDATA[[]]>  </DIV>"
        ) shouldBe false
        isValid("<DIV>  unmatched start tag <B>  and unmatched end tag </C>  </DIV>") shouldBe false
        isValid("<![CDATA[wahaha]]]><![CDATA[]> wahaha]]>") shouldBe false
        isValid("</A></A></A></A>") shouldBe false
    }

}