package xyz.scootaloo.thinking.server.dav.util

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.obj
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import xyz.scootaloo.thinking.lang.*

/**
 * @author flutterdash@qq.com
 * @since 2022/5/7 13:59
 */
object XmlReader : XmlHelper {
    fun parseLockInfo(xml: String): Pair<Boolean, JsonObject> {
        val document = safeParseXml(xml) ?: return false to INVALID_JSON
        val root = document.rootElement
        return true to Json.obj {
            this[DAVCommonLockLabels.lockType] = takeLockType()
            this[DAVCommonLockLabels.lockScope] = root.takeLockScope()
//            this[DAVCommonLabels.owner] = root.takeOwner()
        }
    }

    fun parsePropFind(xml: String): Pair<Boolean, JsonObject> {
        val document = safeParseXml(xml) ?: return true to INVALID_JSON
        val root = document.rootElement
        val props = root.collectChildren(DAVPropFindLabels.prop)
        return true to Json.obj {
            this[DAVPropFindLabels.propName] = root.hasChild(DAVPropFindLabels.propName)
            this[DAVPropFindLabels.props] = Json.array {
                props.forEach {
                    it.takePropFindProp().ifValid(::add)
                }
            }
        }
    }

    private fun Element.takePropFindProp(): Pair<Boolean, JsonObject> {
        return try {
            true to Json.obj {
                this[DAVPropFindLabels.url] = namespaceURI
                this[DAVPropFindLabels.props] = Json.array {
                    elements().map { it.name }.forEach(::add)
                }
            }
        } catch (error: Throwable) {
            false to INVALID_JSON
        }
    }

    private fun Element.takeOwner(): String {
//        val (exists, owner) = access("${DAVCommonLabels.owner}.href") { it.textTrim }
//        if (exists) return owner()
//        return Constant.C_UNKNOWN
        TODO()
    }

    private fun Element.takeLockScope(defScope: String = DAVCommonLockLabels.exclusive): String {
        fun takeScope(ele: Element): String {
            if (ele.hasChild(DAVCommonLockLabels.exclusive)) return DAVCommonLockLabels.exclusive
            if (ele.hasChild(DAVCommonLockLabels.shared)) return DAVCommonLockLabels.shared
            return defScope
        }
        val (exists, scope) = access(DAVCommonLockLabels.lockScope) { takeScope(it) }
        if (exists) return scope()
        return defScope
    }

    private fun takeLockType(): String {
        // https://datatracker.ietf.org/doc/html/rfc4918#section-7
//        return DAVCommonLabels.write
        TODO()
    }

}