package xyz.scootaloo.thinking.lang

import org.junit.jupiter.api.Test
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
    return LoggerFactory.getLogger("service:$name")
}

fun VertxUtils.getLogger(name: String): Logger {
    return LoggerFactory.getLogger("util:$name")
}

fun VertxServiceRegisterCenter.getLogger(name: String): Logger {
    return LoggerFactory.getLogger("vert:$name")
}

fun VertxHttpApplication.getLogger(name: String): Logger {
    return LoggerFactory.getLogger("http:$name")
}

inline fun <T : Any> T?.ifNotNull(block: (T) -> Unit): T? {
    if (this != null) {
        block(this)
    }
    return this
}

fun <T : Any> Pair<Boolean, T>.getOfNull(): T? {
    return if (first) second else null
}

fun <T : Any> Pair<Boolean, T>.getOrElse(def: T): T {
    return if (first) second else def
}

inline fun <T : Any> T?.ifNull(block: () -> Unit) {
    if (this == null) {
        block()
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

fun <K, V> MutableMap<K, V>.putIfAbsent(key: K, lazy: () -> V): V {
    val exists = this[key]
    if (exists == null) {
        val newValue = lazy()
        this[key] = newValue
        return newValue
    }
    return exists
}

interface Nameable {
    val name: String
}

fun interface Factory<In, Out> {
    operator fun invoke(input: In): Out
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

abstract class StateHolder<S, T> {

    abstract var state: S
    abstract var data: T?

}

data class StateValueHolder<S, T>(
    val state: S,
    val holder: ValueHolder<T>,
) {
    companion object {
        fun <S, T> empty(state: S): StateValueHolder<S, T> {
            return StateValueHolder(state, ValueHolder.empty())
        }
    }
}

private class StandardUnitTest : TestDsl {

    @Test
    fun testValueHolder() {
        StateValueHolder<Int, String>(
            0, ValueHolder.empty()
        )
    }

}