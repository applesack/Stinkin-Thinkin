package xyz.scootaloo.thinking.util

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.ifNotNull
import java.util.*

/**
 * 在最少使用的键值对被淘汰时可以将此键值对返回的LRU实现
 *
 * 功能增强, 可以根据[V]以及[counter]来计算缓存的实际数量, 而不是简单判断集合元素个数
 *
 * [put] 如果插入键值对没有到达[maxSize]指定的上限, 则返回null,
 * 否则返回被淘汰的键值对(由于可能淘汰多个键值对, 所以返回的是列表)
 *
 * @author flutterdash@qq.com
 * @since 2022/5/20 15:13
 */
class CountableLRUCache<K : Any, V : Any>(
    private val maxSize: Int, private val counter: (V) -> Int = { 1 },
) {
    private val map = HashMap<K, Node<K, V>>()
    private val head = Node<K, V>()
    private val tail = Node<K, V>()

    private var count = 0

    init {
        head.next = tail
        tail.prev = head
    }

    operator fun contains(key: K): Boolean {
        return key in map
    }

    operator fun get(key: K): V? {
        return map[key].ifNotNull(::moveToHead)?.value
    }

    fun put(key: K, value: V): List<Pair<K, V>>? {
        map[key].ifNotNull {
            count -= counter(it.value)
            count += counter(value)
            it.value = value
            moveToHead(it)
            return@put null
        }

        val space = counter(value)
        val needSpace = maxSize - space
        if (count > needSpace) {
            return removeEldest(space).apply {
                doPut(key, value)
            }
        } else {
            doPut(key, value)
        }

        return null
    }


    private fun removeEldest(space: Int = -1): List<Pair<K, V>> {
        if (space < 0) {
            return listOf(removeTail().pair())
        }

        val needSpace = maxSize - space
        if (needSpace < 0) {
            return clearAndDisplayElements()
        }

        val eliminations = LinkedList<Pair<K, V>>()
        while (count > needSpace) {
            eliminations.add(removeEldestEntry())
        }

        return eliminations
    }

    private fun clearAndDisplayElements(): List<Pair<K, V>> {
        val elements = LinkedList<Pair<K, V>>()
        while (map.isNotEmpty()) {
            elements.add(removeEldestEntry())
        }
        return elements
    }

    private fun removeEldestEntry(): Pair<K, V> {
        val eldest = removeTail()
        map.remove(eldest.key)
        return eldest.pair()
    }

    private fun doPut(key: K, value: V) {
        val node = createNode(key, value)
        map[key] = node
        addToHead(node)
    }

    private fun moveToHead(node: Node<K, V>) {
        node.delete()
        addToHead(node)
    }

    private fun addToHead(node: Node<K, V>) {
        count += counter(node.value)

        node.prev = head
        node.next = head.next

        head.next.prev = node
        head.next = node
    }

    private fun removeTail(): Node<K, V> {
        val rsl = tail.prev
        return rsl.delete()
    }

    private fun Node<K, V>.delete(): Node<K, V> {
        count -= counter(value)

        prev.next = next
        next.prev = prev
        return this
    }

    private fun createNode(key: K, value: V): Node<K, V> {
        val node = Node<K, V>()
        node.key = key
        node.value = value
        return node
    }

    private class Node<K : Any, V : Any> {
        lateinit var key: K
        lateinit var value: V
        lateinit var prev: Node<K, V>
        lateinit var next: Node<K, V>

        fun pair(): Pair<K, V> {
            return key to value
        }

        override fun toString(): String {
            return "Node(key=$key, value=$value)"
        }
    }
}

private class CountableLRUCacheUnitTest : TestDsl {

    @Test
    fun test() {
        // 基本用法: 设置缓存容量, 然后每次调用put可以将最旧未使用的元素删除并返回
        val cache = CountableLRUCache<Int, Int>(3)
        cache.put(1, 1).log() // null
        cache.put(2, 2).log() // null
        cache.put(3, 3).log() // null
        cache.put(1, 1).log() // null
        cache.put(5, 5).log() // [(2, 2)]  // 键为2的键值对最久未访问, 被淘汰
    }

    @Test
    fun advanced() {
        // 进阶用法:
        // 设定一块空间容量为1000, 每次存放键值对时, 值将消耗空间, 当剩余空间不足存放新键值对时, 删除最久未访问的键值对
        val cache = CountableLRUCache<Int, Int>(1000) { it }
        // 当前已存 0
        cache.put(1, 300).log() // 增加300, 剩余容量700
        cache.put(2, 300).log() // 增加300, 剩余容量400
        cache.put(3, 400).log() // 增加400, 剩余容量0
        cache.put(2, 200).log() // 将2的空间修改为200, 然后剩余容量变为100
        cache.put(4, 500).log() // 新增500, 容量不足, 于是最久未访问的1和3被淘汰, 剩余容量300 (100 + 300 + 400 - 500)
    }

}