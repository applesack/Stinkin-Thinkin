package xyz.scootaloo.thinking.struct

/**
 * 斯马特前缀树
 *
 * 1. 这个类的本质是存储键值对
 * 2. 使用的数据结构是树, 树的基本单位是节点; 节点存放了实际的值, 而节点所在的路径是键
 * 3. 一个节点可能存放了多个值
 * 
 * @author flutterdash@qq.com
 * @since 2022/4/8 12:14
 */
class SmartTrie(
    val separator: Char = '/',
    val skipBlank: Boolean = true
) {

    private val root = SentryNode("*")

    fun access(path: String, param: Any = Unit) {

    }

    fun insert(path: String, filter: ModeFilter<Any, Any>) {

    }

    fun delete(path: String,  filter: ModeFilter<Any, Any>) {
    }

    companion object {
        fun split(str: String, separator: Char, skipBlank: Boolean): List<String> {
            val buff = StringBuilder()
            val items = ArrayList<String>()
            for (ch in str) {
                if (ch == separator) {
                    if (skipBlank && buff.isBlank()) {
                        buff.clear()
                        continue
                    }
                    items.add(buff.toString())
                    buff.clear()
                } else {
                    buff.append(ch)
                }
            }
            return items
        }
    }

    interface ModeFilter<In, Out> {
        fun sample(): In
        fun accept(input: Any): Boolean
        fun doFilter(input: In): Out
        fun filter(input: Any): Out {
            return doFilter(input as In)
        }
    }

    class SentryNode(
        val itemKey: String,
        val subItems: SubItemManager = SubItemManager(),
        private var filters: ArrayList<ModeFilter<Any, Any>> = FAKE_FILTERS,
        private val parent: SentryNode? = null
    ) {
        fun parent(): SentryNode {
            return parent ?: FAKE_NODE
        }

        companion object {
            val FAKE_FILTERS = ArrayList<ModeFilter<Any, Any>>(0)
            val FAKE_NODE: SentryNode = SentryNode("")
        }
    }

    class SubItemManager(
        private var items: Array<Entry> = EMPTY_ARR
    ) {
        fun get(key: String) {

        }

        fun put(key: String, value: SentryNode) {

        }

        companion object {
            val EMPTY_ARR = Array(0) { Entry("", SentryNode.FAKE_NODE) }
        }
    }

    class Entry(val key: String, val value: SentryNode)
}