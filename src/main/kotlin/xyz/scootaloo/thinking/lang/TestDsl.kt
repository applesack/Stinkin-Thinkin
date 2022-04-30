package xyz.scootaloo.thinking.lang

/**
 * @author flutterdash@qq.com
 * @since 2022/4/30 22:03
 */

interface TestDsl {

    infix fun <T> T?.shouldBe(expect: T?) {
        if (this != expect)
            throw ResultMistakeException(this, expect)
    }

    fun <T> T?.log() {
        println(this)
    }

    infix fun <T> T.check(block: (T) -> Unit) {
        block(this)
        "test pass".log()
    }

}

class ResultMistakeException(actual: Any?, expect: Any?) : RuntimeException(
    "`$actual` returns, bus `$expect` expected"
)