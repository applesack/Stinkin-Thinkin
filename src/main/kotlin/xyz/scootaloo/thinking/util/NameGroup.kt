package xyz.scootaloo.thinking.util

import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.Nameable
import xyz.scootaloo.thinking.lang.TestDsl
import java.lang.IndexOutOfBoundsException
import kotlin.jvm.Throws
import kotlin.math.abs

/**
 * # 存储键值对, 用于在一些特定场景代替HashMap; (使用String作为键的时候, [Nameable])
 *
 * ## 特性
 * 调用[has], [add]操作后, 会记忆查找的结果, 然后调用[get]后可以直接将结果返回, 在增加少查找多的场景可以大幅度提速;
 * 仅使用一个数组作为数据结构, 当键值对较多时可以有效节省空间;
 *
 * ## 原理
 * 将内部元素按照`hash`值升序排序, 使得查找时可以按照二分查找快速定位, 当有多个元素`hash`值一致时,
 * 先比较元素之间的长度, 可以快速跳过一些不可能相等的值, 然后再比较内容, 从元素的两端进行双指针遍历
 *
 * ## 使用到的算法
 * 哈希, 二分查找, 双指针查找, 两端扩散
 *
 * @author flutterdash@qq.com
 * @since 2022/4/30 21:51
 */
class NameGroup<T : Nameable> {

    private var group: Array<Nameable> = arrayOf()
    private val size: Int get() = group.size
    private var findIdx = -1

    fun has(name: String): Boolean {
        if (group.isEmpty()) {
            return false
        }

        val nameHash = hash(name)
        val pivot = binarySearch(nameHash)
        if (pivot < 0)
            return false

        val limit = size
        var l = pivot
        var r = pivot + 1
        var selected: String
        while (l >= 0 || r < limit) {
            if (l >= 0) {
                selected = group[l].name
                if (hash(selected) != nameHash) {
                    l = -1
                } else {
                    if (
                        selected.length == name.length &&
                        doublePointerMatch(selected, name)
                    ) {
                        findIdx = l
                        return true
                    }
                    l--
                }
            }

            if (r < limit) {
                selected = group[r].name
                if (hash(selected) != nameHash) {
                    r = limit
                } else {
                    if (
                        selected.length == name.length &&
                        doublePointerMatch(selected, name)
                    ) {
                        findIdx = r
                        return true
                    }
                    r++
                }
            }
        }

        return false
    }

    @Throws(IndexOutOfBoundsException::class)
    fun get(): T {
        @Suppress("UNCHECKED_CAST")
        return group[findIdx] as T
    }

    fun add(member: T) {
        if (size == 0) {
            group = arrayOf(member)
            findIdx = 0
            return
        }
        if (has(member.name)) {
            group[findIdx] = member
            return
        }
        val suitable = suitablePlace(member.name)
        val newGroup = Array(size + 1) { idx ->
            if (idx < suitable) {
                group[idx]
            } else if (idx > suitable) {
                group[idx - 1]
            } else {
                member
            }
        }
        findIdx = suitable
        group = newGroup
    }

    fun del(member: String) {
        if (!has(member)) {
            return
        }

        if (size == 1) {
            group = arrayOf()
            return
        }

        val newGroup = Array(size - 1) { idx ->
            if (idx < findIdx) {
                group[idx]
            } else {
                group[idx + 1]
            }
        }
        group = newGroup
    }

    private fun suitablePlace(member: String): Int {
        val h = hash(member)
        for (idx in group.indices) {
            if (hash(group[idx].name) > h)
                return idx
        }
        return size
    }

    private fun doublePointerMatch(a: String, b: String): Boolean {
        var head = 0
        var tail = a.length - 1
        while (head < tail) {
            if (a[head] == b[head]) {
                head++
            } else {
                return false
            }

            if (a[tail] == b[tail]) {
                tail--
            } else {
                return false
            }
        }

        if (head == tail)
            return a[head] == b[head]
        return true
    }

    private fun binarySearch(hash: Int): Int {
        var high = size - 1
        var low = 0

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = hash(group[mid].name)

            if (midVal < hash) {
                low = mid + 1
            } else if (midVal > hash) {
                high = mid - 1
            } else {
                return mid
            }
        }

        return -1
    }

    private fun hash(str: String): Int {
        return abs(str.hashCode())
    }

    override fun toString(): String {
        return "NameGroup(group=${group.contentToString()}, findIdx=$findIdx)"
    }
}

private class NameGroupUnitTest : TestDsl {

    @Test
    fun test() {
        val group = NameGroup<Name>()

        // set

        group.add(Name("1112345454664654545"))
        group.add(Name("1111111111111111111111111111"))
        group.add(Name("2"))
        group.add(Name("7&&&&&&&&&&&&&%%%%%%%%%%%%%%%%----------))((("))
        group.add(Name(""))
        group.add(Name("555555555FFFbEF^!!!!!!!;';l;;';',"))

        // get

        group.has("1112345454664654545") shouldBe true
        group.has("1111111111111111111111111111") shouldBe true
        group.has("2") shouldBe true
        group.has("7&&&&&&&&&&&&&%%%%%%%%%%%%%%%%----------))(((") shouldBe true
        group.has("") shouldBe true
        group.has("555555555FFFbEF^!!!!!!!;';l;;';',") shouldBe true

        group.has("1") shouldBe false
        group.has("123") shouldBe false
        group.has("7&&&&&&&&&&&&&%%%%%%%%%%%%%%%%-----x-----))(((") shouldBe false
    }

    data class Name(override val name: String) : Nameable

}