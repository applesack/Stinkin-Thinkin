package xyz.scootaloo.thinking.lang

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author flutterdash@qq.com
 * @since 2022/4/27 17:30
 */

fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}

fun VertxService.getLogger(name: String): Logger {
    return LoggerFactory.getLogger(name)
}

inline fun <T : Any> T?.ifNotNull(block: (T) -> Unit) {
    if (this != null) {
        block(this)
    }
}

infix fun String.like(other: String): Boolean {
    if (this.length != other.length)
        return false
    return this.startsWith(other, true)
}

infix fun <T : Any> MutableList<T>.add(data: T): Boolean {
    return this.add(data)
}

infix fun <K : Any> MutableMap<K, *>.remove(key: K): Any? {
    return this.remove(key)
}

fun <T : Any> List<T>.copy(): List<T> {
    return ArrayList<T>(this.size).apply { this@copy.forEach { this@apply.add(it) } }
}

val Pair<Boolean, *>.isValid get() = this.first

inline fun <T : Any> Pair<Boolean, T>.ifValid(block: (T) -> Unit) {
    if (this.first) {
        block(this.second)
    }
}

interface Nameable {
    val name: String
}

fun interface Factory<T> {
    operator fun invoke(type: String): T
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