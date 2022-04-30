package xyz.scootaloo.thinking.lang

/**
 * @author flutterdash@qq.com
 * @since 2022/4/27 17:30
 */

inline fun <T> T?.ifNotNull(block: (T) -> Unit) {
    if (this != null) {
        block(this)
    }
}

infix fun String.like(other: String): Boolean {
    if (this.length != other.length)
        return false
    return this.startsWith(other, true)
}

interface Nameable {
    val name: String
}

class ValueHolder<T>(private val value: T?) {
    operator fun invoke(): T {
        return value!!
    }
    companion object {
        fun <T> empty(): ValueHolder<T> {
            return ValueHolder(null)
        }
    }
}