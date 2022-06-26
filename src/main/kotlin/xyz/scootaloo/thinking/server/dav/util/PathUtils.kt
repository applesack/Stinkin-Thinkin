package xyz.scootaloo.thinking.server.dav.util

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lib.HttpHeaderHelper

/**
 * @author flutterdash@qq.com
 * @since 2022/5/15 11:00
 */
object PathUtils : HttpHeaderHelper {

    fun displayName(path: String): String {
        val sep = if ('/' in path) '/' else '\\'
        val spIdx = path.lastIndexOf(sep)
        return path.substring(spIdx + 1)
    }

    fun part(path: String): Pair<String, String> {
        val display = displayName(path)
        if ('.' !in display) {
            return display to ""
        }
        val idx = path.indexOf('.')
        val main = path.substring(0, idx)
        val ext = path.substring(idx + 1)
        return main to ext
    }

    private fun fullPart(path: String): Pair<String, String> {
        val (main, ext) = part(path)
        val sep = if ('/' in path) '/' else '\\'
        val spIdx = path.lastIndexOf(sep)
        return if (spIdx >= 0)
            "${path.substring(0, spIdx + 1)}$main" to ext
        else
            main to ext
    }

    fun extractRelativePath(fullPath: String, basePath: String): String {
        return normalize(fullPath.substring(basePath.length))
    }

    fun normalize(path: String): String {
        if (path.isEmpty())
            return "/"
        val idx = path.indexOf('\\')
        val head = path.first()
        if (idx < 0 && head == '/')
            return path
        val buffSize = if (head == '/' || head == '\\') path.length else path.length + 1
        val buff = StringBuilder(buffSize)
        if (buffSize != path.length) {
            buff.append('/')
        }
        for (ch in path) {
            if (ch == '\\')
                buff.append('/')
            else
                buff.append(ch)
        }
        if (buff.length > 1 && buff.last() == '/') {
            buff.setLength(buff.length - 1)
        }

        return buff.toString()
    }

    fun fileContentType(mainName: String): String {
        return contentTypeOf(mainName)
    }

    fun generateNoDuplicateName(original: String): String {
        val (main, ext) = fullPart(original)
        if (main.isNotEmpty() && main.last() == ')') {
            var idx = main.lastIndex - 1
            while (idx >= 0) {
                if (main[idx] == '(')
                    break
                idx--
            }
            if (idx >= 0) {
                val number = try {
                    main.substring(idx + 1, main.lastIndex).toInt()
                } catch (formatError: Throwable) {
                    -1
                }
                if (number >= 1) {
                    val prefix = main.substring(0, idx).trim()
                    return join("$prefix (${number + 1})", ext)
                }
            }
        }
        return join("$main (1)", ext)
    }

    private fun join(main: String, ext: String): String {
        return (if (ext.isEmpty()) main else "${main}.$ext").trim()
    }

}

private class PathUtilsUnitTest : TestDsl {

    @Test
    fun test() {
        PathUtils.extractRelativePath("D:\\abc\\def", "D:/abc").log()
        PathUtils.extractRelativePath("D:\\abc\\def\\12", "D:/abc").log()
    }

    @Test
    fun test2() {
        PathUtils.extractRelativePath(
            "D:\\code\\java\\project\\Stinkin-Thinkin\\home\\.git",
            "D:/code/java/project/Stinkin-Thinkin/home"
        ).log()
    }

    @Test
    fun test3() {
        PathUtils.part("abc").log()
        PathUtils.part("abc.c").log()
        PathUtils.part("abc.cdf").log()
        PathUtils.part(".cdf").log()
    }

    @Test
    fun test4() {
        PathUtils.generateNoDuplicateName(".c") shouldBe "(1).c"
        PathUtils.generateNoDuplicateName("") shouldBe "(1)"
        PathUtils.generateNoDuplicateName("(1)") shouldBe "(2)"
        PathUtils.generateNoDuplicateName("(-1)") shouldBe "(-1) (1)"
        PathUtils.generateNoDuplicateName("(abc)") shouldBe "(abc) (1)"
        PathUtils.generateNoDuplicateName(".(abc)") shouldBe "(1).(abc)"
        PathUtils.generateNoDuplicateName("abc_.(abc)") shouldBe "abc_ (1).(abc)"
        PathUtils.generateNoDuplicateName("abc_ (1).(abc)") shouldBe "abc_ (2).(abc)"
        PathUtils.generateNoDuplicateName(
            "D:\\code\\java\\project\\Stinkin-Thinkin\\home"
        ) shouldBe "D:\\code\\java\\project\\Stinkin-Thinkin\\home (1)"
    }

}