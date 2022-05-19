package xyz.scootaloo.thinking.server.dav.util

import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.slf4j.Logger
import xyz.scootaloo.thinking.lang.ValueHolder
import xyz.scootaloo.thinking.lang.like

/**
 * @author flutterdash@qq.com
 * @since 2022/5/8 12:03
 */
interface XmlHelper {

    fun Element.collectChildTags(): List<String> {
        return elements().map { it.name }
    }

    fun Element.first(child: String): Element? {
        return elements().first { it.name like child }
    }

    fun Element.hasChild(name: String): Boolean {
        return elements().any { it.name like name }
    }

    fun <T> Element.access(path: String, gen: (Element) -> T): Pair<Boolean, ValueHolder<T>> {
        return access(this, path.split('.'), 0, gen)
    }

    fun <T> access(
        ele: Element, pathItems: List<String>,
        idx: Int, gen: (Element) -> T,
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

    fun safeParseXml(log: Logger, xml: String): Document? {
        return try {
            DocumentHelper.parseText(xml)
        } catch (e: Throwable) {
            log.error("en error when parse xml document \n $xml \n", e)
            null
        }
    }

}