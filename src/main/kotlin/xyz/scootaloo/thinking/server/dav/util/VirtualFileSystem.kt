package xyz.scootaloo.thinking.server.dav.util

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import xyz.scootaloo.thinking.lang.ifNotNull
import java.util.*

/**
 * 使用文件夹作为节点生成一个树状结构, 树的每个节点都与文件系统的文件夹一一对应;
 * 这个结构具有如下特性:
 *
 * 1. 如果一个文件夹的路径在树中不存在, 那么文件系统中也不会存在此文件夹(可用来减少文件系统的访问)
 * 2. 访问某目标路径时, 会按顺序依次访问途径的节点(可以用来实现责任链过滤器)
 * 3. 每个节点都有存储锁和规则信息的能力
 *
 * 附加特性:
 *
 * - LRU缓存, 动态维持一定数量的最常用文件的信息, 包括从数据库中整合来的数据
 *
 * @author flutterdash@qq.com
 * @since 2022/4/21 17:03
 */
object VirtualFileSystem {

    val struct = Struct()
    val viewer = Viewer()

    private const val SEPARATOR = '/'
    private val root = SentryNode(SEPARATOR.toString())

    // last-hit-node, status, filename
    private fun search(path: String): Triple<SentryNode, SearchStatus, String> {
        val pathItems = Helper.pathSplit(path, SEPARATOR)
        var currentNode: SentryNode = root
        for (idx in pathItems.indices) {
            val item = pathItems[idx]
            if (currentNode.hasChild(item)) {
                currentNode = currentNode.getChild()
            } else {
                return if (idx == (pathItems.size - 1)) {
                    Triple(currentNode, SearchStatus.END, pathItems.last())
                } else {
                    Triple(currentNode, SearchStatus.MISSING, "")
                }
            }
        }

        return Triple(currentNode, SearchStatus.END, "")
    }

    enum class SearchStatus {
        NODE, END, MISSING
    }

    /**
     * 会修改文件系统结构的一些操作;
     * 如创建/删除文件/文件夹, 移动/复制等
     */
    class Struct private constructor() {
        fun createDirectory(directoryPath: String) {
            val pathItems = Helper.pathSplit(directoryPath, SEPARATOR)
            var currentNode: SentryNode = root
            for (item in pathItems) {
                currentNode = if (currentNode.hasChild(item)) {
                    currentNode.getChild()
                } else {
                    currentNode.addChild(item)
                    currentNode.getChild()
                }
            }
        }

        fun deleteDirectory(directoryPath: String) {
            searchDirectory(directoryPath).ifNotNull {
                it.parent.delChild(it.name)
            }
        }

        private fun searchDirectory(directoryPath: String): SentryNode? {
            val pathItems = Helper.pathSplit(directoryPath, SEPARATOR)
            var currentNode: SentryNode = root
            for (item in pathItems) {
                currentNode = if (currentNode.hasChild(item)) {
                    currentNode.getChild()
                } else {
                    return null
                }
            }
            return currentNode
        }

        companion object {
            operator fun invoke() = Struct()
        }
    }

    /**
     * 不会修改文件系统结构的操作;
     * 如查看文件的属性, 给文件加锁/解锁, 设置/移除属性等
     */
    class Viewer private constructor() {

        private val supportProperties = listOf(
            "author", "creationDate", "displayName", "resourceType", "supportLock"
        )

        fun supportProps() {

        }

        suspend fun findFile() {

        }

        companion object {
            operator fun invoke() = Viewer()
        }
    }

    object LockManager {
        private val lockMapper = HashMap<String, LockRecord>()
        private val expiryMapper = TreeMap<Long, String>()

        /**
         * [9.10.5](https://tools.ietf.org/html/rfc4918#section-9.10.5)
         *
         * +--------------------------+----------------+-------------------+
         * | Current State            | Shared Lock OK | Exclusive Lock OK |
         * +--------------------------+----------------+-------------------+
         * | None                     | True           | True              |
         * | Shared Lock              | True           | False             |
         * | Exclusive Lock           | False          | False*            |
         * +--------------------------+----------------+-------------------+
         */
        fun lock(node: SentryNode, file: String, message: Message<JsonObject>) {
            val currentState = node.getRecord(file)
            if (currentState == null) {

            }
        }

        fun refresh() {

        }

        fun autoGC() {

        }
    }

    object Helper {
        fun pathSplit(path: String, sep: Char): List<String> {
            val buff = StringBuilder()
            val res = ArrayList<String>(4)
            for (ch in path) {
                if (ch == sep || ch == '\\') {
                    if (buff.isNotBlank()) {
                        res.add(buff.toString())
                        buff.clear()
                    }
                } else {
                    buff.append(ch)
                }
            }
            if (buff.isNotBlank())
                res.add(buff.toString())
            return res
        }

        fun normalize(path: String): String {
            return path
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
    }
}