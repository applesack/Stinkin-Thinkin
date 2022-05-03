package xyz.scootaloo.thinking.server.dav

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import xyz.scootaloo.thinking.lang.headers
import xyz.scootaloo.thinking.lang.ifNotNull
import xyz.scootaloo.thinking.lang.Constant
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
 * - LRU缓存, 动态保存500个最常用文件的信息, 包括从数据库中整合来的数据
 * - 动态刷新锁的超时
 *
 * ---
 *
 * 支持 WebDAV 的几种操作
 *
 * - Lock: 加锁, 如果目标文件存在, 则在目标文件所在的文件夹上附加一个锁信息(可以定时删除);
 *     - 输入: 目标文件所在的路径, 锁类型, 超时时间
 *     - 输入: 处理结果(如果处理成功, 则返回该锁的唯一ID, 字符串类型)
 *
 * - Unlock: 解锁, 如果用户是该锁的拥有者, 则解锁成功
 *     - 输入: 目标文件所在的路径
 *     - 输出: 处理结果
 *
 * - PropFind: 属性发现, 根据深度信息, 返回一定范围内的所有文件描述
 *     - 输入: 文件路径, 范围(深度, 可选0,1和无限)
 *     - 输入: 范围内的文件描述
 *
 * - PropPatch: 属性修改, 允许更新,设置或者删除一个属性
 *     - 输入: 文件路径
 *     - 输出: 处理结果()
 *
 * - MKCOL: 创建集合, 创建文件或者文件夹
 *
 * - PUT: 将资源上传到指定路径(替代已经存在的文件)
 *
 * @author flutterdash@qq.com
 * @since 2022/4/21 17:03
 */
object VirtualFileSystem {

    val struct = Struct()
    val viewer = Viewer()
    val options = Options()

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

    class Options private constructor() {
        suspend fun lock(message: Message<JsonObject>) {
//           val (hitNode, state, filename) = search(message.headers[Constant.EB_H_PATH])
//            if (state == SearchStatus.END) {
//                LockManager.lock(hitNode, filename, message)
//            } else {
//                // 客户端尝试锁定不存在的资源, 响应204状态码; 9.10.4
////                message { replyWithHttp204() }
//            }
        }

        fun unlock(message: Message<JsonObject>) {
            message.reply("")
        }

        companion object {
            operator fun invoke(): Options {
                return Options()
            }
        }
    }

    class Viewer private constructor() {
        fun propFind(path: String) {
        }

        fun propPatch(path: String) {
        }

        fun lock(path: String) {
        }

        fun unlock(path: String) {
        }

        fun copy() {
        }

        fun move() {
        }

        fun delete() {
        }

        fun makeCol() {
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