package xyz.scootaloo.thinking.server.dav.util

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import xyz.scootaloo.thinking.lang.ValueHolder
import xyz.scootaloo.thinking.lang.like
import xyz.scootaloo.thinking.lang.set
import xyz.scootaloo.thinking.lang.Constant

/**
 * 1. -> 接收客户端请求: 在handler中解析xml, 将xml信息解析为json
 * 2. <- 将json作为消息发送到eventbus, 在总线处理器中将json转换为实体类进行最终处理
 * 3. -> 总线处理器将处理结果使用json格式保存, 并把处理结果返回给handler
 * 4. <- handler接收到处理结果, 并把处理结果转化为xml, 发送给客户端
 *
 * request -> xml原始类型1 -> json中间类型1 -> 实际java对象
 * json中间结果类型2 -> xml最终类型2 -> response
 *
 * 需要实现3次转换
 * xml1 -> json1         1. 接收客户端请求时解析, 可以对格式进行预处理
 * json1 -> javaObject   2. 文件服务(异步)对中间结果进行转换, 内部调用
 * json2 -> xml2         3. 转换文件服务的处理结果
 *
 * @author flutterdash@qq.com
 * @since 2022/4/26 23:15
 */
private typealias Labels = DAVCommonLabels
private typealias LockLabels = DAVCommonLockLabels

private object DAVCommonLabels {
    const val owner = "owner"
    const val write = "write"
}

object DAVTopLabels {
    const val lockInfo = "lockInfo"
}

/**
 * ```json
 * {
 *     "LockType": "write",
 *     "LockScope": "Shared",
 *     "Owner": "Username"
 * }
 * ```
 */
private object DAVCommonLockLabels {
    const val lockScope = "LockScope"
    const val lockType = "LockType"
    const val shared = "shared"
    const val exclusive = "exclusive"
}

interface DAVStructParser {
    fun reqXml2json(ctx: RoutingContext): Triple<Boolean, Map<String, String>, JsonObject>
    fun respJson2xml(json: JsonObject): String
}

interface DAVXmlHelper {
    fun parseLockInfo(xml: String): Pair<Boolean, JsonObject> {
        val document = safeParseXml(xml) ?: return false to INVALID_JSON
        val root = document.rootElement
        return true to Json.obj {
            this[DAVCommonLockLabels.lockType] = root.takeLockType()
            this[DAVCommonLockLabels.lockScope] = root.takeLockScope()
            this[DAVCommonLabels.owner] = root.takeOwner()
        }
    }

    fun Element.takeOwner(): String {
        val (exists, owner) = access("${DAVCommonLabels.owner}.href") { it.textTrim }
        if (exists) return owner()
        return Constant.C_UNKNOWN
    }

    fun Element.takeLockScope(defScope: String = DAVCommonLockLabels.exclusive): String {
        fun takeScope(ele: Element): String {
            val names = ele.collectChildren(
                DAVCommonLockLabels.shared, DAVCommonLockLabels.exclusive
            ).map { it.name }
            if (DAVCommonLockLabels.shared in names) return DAVCommonLockLabels.shared
            if (DAVCommonLockLabels.exclusive in names) return DAVCommonLockLabels.exclusive
            return defScope
        }
        val (exists, scope) = access(DAVCommonLockLabels.lockScope) { takeScope(it) }
        if (exists) return scope()
        return defScope
    }

    fun Element.takeLockType(): String {
        // https://datatracker.ietf.org/doc/html/rfc4918#section-7
        return DAVCommonLabels.write
    }

    private fun Element.collectChildren(vararg names: String): List<Element> {
        val collection = ArrayList<Element>()
        for (ele in elements()) {
            if (names.any { it like ele.name }) {
                collection.add(ele)
            }
        }
        return collection
    }

    private fun <T> Element.access(path: String, gen: (Element) -> T): Pair<Boolean, ValueHolder<T>> {
        return access(this, path.split('.'), 0, gen)
    }

    private fun <T> access(
        ele: Element, pathItems: List<String>,
        idx: Int, gen: (Element) -> T
    ): Pair<Boolean, ValueHolder<T>> {
        if (idx > pathItems.size)
            return false to ValueHolder.empty()
        if (idx == (pathItems.lastIndex)) {
            return true to ValueHolder(gen(ele))
        }

        val selected = pathItems[idx]
        for (child in ele.elements()) {
            if (child.name like selected) {
                return access(child, pathItems, idx + 1, gen)
            }
        }

        return false to ValueHolder.empty()
    }

    private fun safeParseXml(xml: String): Document? {
        return try {
            DocumentHelper.parseText(xml)
        } catch (e: Throwable) {
            null
        }
    }

    companion object {
        val INVALID_JSON = JsonObject()
    }
}

object LockJsonStruct : DAVStructParser {
    override fun reqXml2json(ctx: RoutingContext): Triple<Boolean, Map<String, String>, JsonObject> {
        TODO("Not yet implemented")
    }

    override fun respJson2xml(json: JsonObject): String {
        TODO("Not yet implemented")
    }

    fun instantiate(message: Message<JsonObject>): Pair<Boolean, LockRecord> {
        val json = message.body()
        val scope = json.getString(DAVCommonLockLabels.lockScope)
        val owner = json.getString(DAVCommonLabels.owner)
        return true to (if (scope like "shared") {
            SharedLockRecord().apply { addHolder(owner) }
        } else {
            ExclusiveLockRecord(owner)
        })
    }
}