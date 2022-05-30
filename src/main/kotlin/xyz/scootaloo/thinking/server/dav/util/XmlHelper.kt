package xyz.scootaloo.thinking.server.dav.util

import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.slf4j.Logger
import xyz.scootaloo.thinking.lang.ValueHolder
import xyz.scootaloo.thinking.lang.like

/**
 * ## Xml解析助手, 提供一些便捷的方法来解析xml文档
 *
 * @author flutterdash@qq.com
 * @since 2022/5/8 12:03
 */
interface XmlHelper {

    /**
     * ## 获取xml标签内所有嵌套的子标签(不包含孙子标签)
     */
    fun Element.collectChildTags(): List<String> {
        return elements().map { it.name }
    }

    /**
     * ## 获取xml标签内嵌套的第一个标签对象
     *
     * @param child 需要获取的子标签的标签名, 会自动忽略大小写
     * @return
     * 根据[child]查找指定的标签对象, 如果存在多个则返回第一个;
     * 如果不存在则返回null
     */
    fun Element.first(child: String): Element? {
        return elements().first { it.name like child }
    }

    /**
     * ## 判断当前xml标签内是否存在一个名为[name]的子标签
     *
     * @param name 子标签名, 会自动忽略大小写
     * @return 是否存在
     */
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

    /**
     * ## 安全地解析一段xml文档, 不会抛出异常, 当解析发生错误时返回null值
     *
     * @param log 当解析失败时, 可以通过这个log输出错误信息
     * @param xml 需要解析的xml内容
     */
    fun safeParseXml(log: Logger, xml: String): Document? {
        return try {
            DocumentHelper.parseText(xml)
        } catch (e: Throwable) {
            log.error("en error when parse xml document \n $xml \n", e)
            null
        }
    }

}