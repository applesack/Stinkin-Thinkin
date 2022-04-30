package xyz.scootaloo.thinking.struct.dav

import xyz.scootaloo.thinking.lang.Nameable
import xyz.scootaloo.thinking.util.NameGroup
import java.util.LinkedList

/**
 * @author flutterdash@qq.com
 * @since 2022/4/23 14:58
 */

class RuleRecord

class FileMarkChunk(
    val lock: LockRecord,
    val rule: RuleRecord
)

class SentryNode(
    override val name: String,
    p: SentryNode? = null,
    private var records: Map<String, FileMarkChunk> = FAKE_MAPPER,
    private val children: NameGroup<SentryNode> = NameGroup()
) : Nameable {
    val parent: SentryNode = p ?: this

    fun hasChild(name: String) = children.has(name)
    fun getChild(): SentryNode = children.get()
    fun delChild(name: String) = children.del(name)
    fun addChild(member: String) = children.add(SentryNode(member, this))

    fun getRecord(file: String): FileMarkChunk? = records[file]

    fun fullPath(sep: Char): String {
        val pathItems = LinkedList<String>()
        var cur = this
        while (cur.parent != this) {
            pathItems.addFirst(cur.name)
            cur = cur.parent
        }
        return pathItems.joinToString(sep.toString())
    }

    override fun toString(): String {
        return "Sentry('$name')"
    }

    companion object {
        private val FAKE_MAPPER = HashMap<String, FileMarkChunk>(0)
    }
}