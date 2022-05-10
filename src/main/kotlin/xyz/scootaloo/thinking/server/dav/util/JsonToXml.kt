package xyz.scootaloo.thinking.server.dav.util

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.Namespace
import org.dom4j.QName
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.lang.ifNotNull
import java.util.*

/**
 * @author flutterdash@qq.com
 * @since 2022/5/8 14:02
 */
object JsonToXml {

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
}

class JsonToXmlTest : TestDsl {

    @Test
    fun test0() {
        val json = """
        {
            "response": [{
                "href": "/container/",
                "propStat": [{
                    "prop": {
                        "bigBox": {
                            "boxType": "Box Type A"
                        },
                        "author": {
                            "name": "Hadrian"
                        },
                        "creationDate": "1997-12-01T17:42:21-08:00",
                        "displayName": "Example collection",
                        "resourceType": {}
                    },
                    "status": "HTTP/1.1 200 OK"
                },
                {
                    "prop": {
                         "DingALing": {},
                         "Random": {}
                     },
                     "responseDescription": "The user does not have access to the DingALing property."
                }]
            }],
            "responseDescription": "There has been an access violation error."
        }
        """.trimIndent()
        JsonToXml.convert(JsonObject(json), "propFind").log()
    }

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