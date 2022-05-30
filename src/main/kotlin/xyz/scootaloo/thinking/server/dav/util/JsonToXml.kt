package xyz.scootaloo.thinking.server.dav.util

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.Namespace
import org.dom4j.QName
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.ifNotNull
import xyz.scootaloo.thinking.lang.set
import java.util.*

/**
 * sample1
 * ```json
 * {
 *     "input": "abc"
 * }
 * ```
 * convert, shell="div"
 * ```xml
 * <div>
 *     <input>abc</input>
 * </div>
 * ```
 * ---------------------------------------------
 * sample2
 * ```json
 * {
 *     "input": {}
 * }
 * ```
 * convert, shell="div"
 * ```xml
 * <div>
 *     <input/>
 * </div>
 * ```
 * ---------------------------------------------
 * sample3
 * ```json
 * {
 *     "input": {
 *         "-name": "test",
 *         "#text": "value"
 *     }
 * }
 * ```
 * convert, shell="div"
 * ```xml
 * <div>
 *     <input name="test">value</input>
 * </div>
 * ```
 * @author flutterdash@qq.com
 * @since 2022/5/8 14:02
 */
object JsonToXml {

    fun closedTag() = JsonObject()

    fun textTag(text: String, vararg attrs: Pair<String, String>): JsonObject {
        return Json.obj {
            this[Symbol.text] = text
            for (attr in attrs) {
                this["${Symbol.attr}${attr.first}"] = attr.second
            }
        }
    }

    fun convert(json: JsonObject, shell: String, uri: String = "DAV:"): String {
        val document = DocumentHelper.createDocument()
        val root = document.addElement(QName(shell.lowercase(Locale.getDefault()), Namespace("D", uri)))
        json.traverse(root, root.namespace)
        return document.asXML()
    }

    private fun JsonObject.traverse(root: Element, namespace: Namespace) {
        for ((key, value) in this) {
            switch(root, key, value, namespace)
        }
    }

    private fun switch(root: Element, name: String, data: Any, namespace: Namespace) {
        if (name == Symbol.text) {
            root.addText(data.toString())
            return
        }
        if (name.startsWith(Symbol.attr)) {
            root.addAttribute(name.substring(Symbol.attr.length), data.toString())
            return
        }

        when (data) {
            is JsonObject -> {
                (data as? JsonObject).ifNotNull {
                    it.traverse(root.flattening(name, namespace), namespace)
                }
            }
            is JsonArray -> {
                (data as? JsonArray).ifNotNull {
                    for (item in it) {
                        switch(root, name, item, namespace)
                    }
                }
            }
            else -> {
                root.flattening(name, namespace).addText(data.toString())
            }
        }
    }

    private fun Element.flattening(name: String, namespace: Namespace): Element {
        return addElement(QName(name.lowercase(Locale.getDefault()), namespace))
    }

    private object Symbol {
        const val text = "#text"
        const val attr = "-"
    }

}

private class JsonToXmlTest : TestDsl {

    @Test
    fun testPropName() {

    }

    @Test
    fun test1() {
        val json = """
            {
                "item": [
                    "name", "age", "class"
                ]
            }
        """.trimIndent()
        JsonToXml.convert(JsonObject(json), "test").log()
    }

    @Test
    fun testBuildCloseLabel() {
        val document = DocumentHelper.createDocument()
        val root = document.addElement("root")
        root.addElement("close")
        val notClose = root.addElement("not-close")
        notClose.addText("context-comment")
        document.asXML().log()
    }

    @Test
    fun testBuildArray() {
        val document = DocumentHelper.createDocument()
        val root = document.addElement("root")
        root.addElement("item").addText("1")
        root.addElement("item").addText("2")
        document.asXML().log()
    }

    @Test
    fun testBuildNamespaceXml() {
        val document = DocumentHelper.createDocument()
        val root = document.addElement(QName("root", Namespace("D", "DAV:")))
        val namespace = root.namespace
        val item1 = root.addElement(QName("item1", namespace))
        item1.addElement(QName("item2", namespace))
        document.asXML().log()
    }

}