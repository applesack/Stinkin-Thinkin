package xyz.scootaloo.thinking.util

import xyz.scootaloo.thinking.lang.Nameable

/**
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

        val pivot = binarySearch(name.length)
        if (pivot < 0)
            return false

        val limit = size
        var l = pivot
        var r = pivot + 1
        var selected: String
        while (l >= 0 || r < limit) {
            if (l >= 0) {
                selected = group[l].name
                if (
                    selected.hashCode() == name.hashCode() &&
                    doublePointerMatch(selected, name)
                ) {
                    findIdx = l
                    return true
                }
                if (selected.length != name.length) {
                    l = -1
                } else {
                    l--
                }
            }

            if (r < limit) {
                selected = group[r].name
                if (selected.length != name.length) {
                    r = limit
                    continue
                }
                if (
                    selected.hashCode() == name.hashCode() &&
                    doublePointerMatch(selected, name)
                ) {
                    findIdx = r
                    return true
                }
                r++
            }
        }

        return false
    }

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
        val suitable = suitablePlace(member.name.length)
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

    private fun suitablePlace(len: Int): Int {
        for (idx in group.indices) {
            if (group[idx].name.length > len)
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

    private fun binarySearch(target: Int): Int {
        var high = size - 1
        var low = 0

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = group[mid].name.length

            if (midVal < target) {
                low = mid + 1
            } else if (midVal > target) {
                high = mid - 1
            } else {
                return mid
            }
        }

        return -1
    }

    override fun toString(): String {
        return "NameGroup(group=${group.contentToString()}, findIdx=$findIdx)"
    }
}