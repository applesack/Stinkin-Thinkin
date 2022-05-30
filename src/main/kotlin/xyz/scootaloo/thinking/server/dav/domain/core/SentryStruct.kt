package xyz.scootaloo.thinking.server.dav.domain.core

import xyz.scootaloo.thinking.lang.Nameable
import xyz.scootaloo.thinking.lang.remove
import xyz.scootaloo.thinking.server.dav.util.PathUtils
import xyz.scootaloo.thinking.util.NameGroup
import java.util.LinkedList

/**
 * @author flutterdash@qq.com
 * @since 2022/4/23 14:58
 */

class RuleRecord

data class FileMarkChunk(
    var lock: FileLock = UnreachableFileLock,
    val etag: List<String> = ArrayList()
)

class SentryNode(
    override val name: String,
    p: SentryNode? = null,
    private var records: MutableMap<String, FileMarkChunk> = FAKE_MAPPER,
    private val children: NameGroup<SentryNode> = NameGroup()
) : Nameable {
    private val parent: SentryNode = p ?: this

    fun hasChild(name: String) = children.has(name)
    fun getChild(): SentryNode = children.get()
    fun delChild(name: String) = children.del(name)
    fun addChild(member: String) = children.add(SentryNode(member, this))

    fun getRecord(file: String = ""): FileMarkChunk? = records[file]
    fun putRecord(file: String, record: FileMarkChunk) {
        if (records == FAKE_MAPPER) {
            records = HashMap()
        }
        records[file] = record
        if (needRemove(record)) {
            records.remove(file)
            if (records.isEmpty()) {
                records = FAKE_MAPPER
            }
        }
    }

    fun fullPath(): String {
        val pathItems = LinkedList<String>()
        var cur = this
        while (cur.parent != this) {
            pathItems.addFirst(cur.name)
            cur = cur.parent
        }
        return PathUtils.normalize(pathItems.joinToString("/"))
    }

    private fun needRemove(record: FileMarkChunk): Boolean {
        val (lock, eTags) = record
        return (lock == UnreachableFileLock && eTags.isEmpty())
    }

    override fun toString(): String {
        return "Sentry('$name')"
    }

    companion object {
        private val FAKE_MAPPER = HashMap<String, FileMarkChunk>(0)
    }
}