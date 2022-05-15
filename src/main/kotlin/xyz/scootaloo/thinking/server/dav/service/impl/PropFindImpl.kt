package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.eventbus.Message
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.awaitBlocking
import jdk.jfr.Event
import org.dom4j.Document
import org.dom4j.Element
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.DAVPropFindService
import xyz.scootaloo.thinking.server.dav.service.FileService
import xyz.scootaloo.thinking.server.dav.util.DAVCommonLabels
import xyz.scootaloo.thinking.server.dav.util.JsonToXml
import xyz.scootaloo.thinking.server.dav.util.XmlHelper

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 20:41
 */
object PropFindImpl : SingletonVertxService(), DAVPropFindService, EventbusMessageHelper {

    val log by lazy { getLogger("PropFind") }

    override var context = WebDAVContext.file

    override suspend fun handle(ctx: RoutingContext) {
        val arguments = resolveRequestArguments(ctx)
        val result = eb.callService(InternalProtocol.propFind, arguments).await()
        ctx.smartReply(result.body())
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.propFind) {
            PropFind.handle(it, fs)
        }

        log.info("eventbus PropFind service ready; current context: $contextName")
    }

    private suspend fun resolveRequestArguments(ctx: RoutingContext): JsonObject {
        return awaitBlocking { Resolver.resolveRequestBody(ctx) }
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val propFind = "$prefix:propFind"
    }

    private object Labels : DAVCommonLabels() {
        const val prop = "prop"
        const val propName = "propName"
        const val allProp = "allProp"
        const val url = "url"
        const val props = "props"
        const val propFind = "propFind"
        const val response = "response"
    }

    private object Resolver : XmlHelper {
        /**
         * ```json
         * {
         *     "subject": string,
         *     "depth": string,
         *     "propFind": {
         *         "allProp": bool,
         *         "propName": bool,
         *         "props": [string]
         *     }
         * }
         * ```
         */
        fun resolveRequestBody(ctx: RoutingContext): JsonObject {
            val result = Json.obj { resolveHeader(ctx) }
            val document = safeParseXml(ctx.bodyAsString) ?: return result
            return result.resolveXmlBody(document)
        }

        private fun JsonObject.resolveHeader(ctx: RoutingContext): JsonObject {
            this[Constant.SUBJECT] = ctx.pathParam("*")
            this[Labels.depth] = ctx.request().headers()[Labels.depth]
            return this
        }

        private fun JsonObject.resolveXmlBody(document: Document): JsonObject {
            val root = document.rootElement
            val props = root.collectChildren(Labels.prop)
            this[Labels.propFind] = Json.obj {
                this[Labels.allProp] = root.hasChild(Labels.allProp)
                this[Labels.propName] = root.hasChild(Labels.propName)
                this[Labels.props] = Json.array {
                    props.forEach {
                        it.takePropFindProp().ifValid(::add)
                    }
                }
            }
            return this
        }

        private fun Element.takePropFindProp(): Pair<Boolean, JsonObject> {
            return try {
                true to Json.obj {
                    this[Labels.url] = namespaceURI
                    this[Labels.props] = Json.array {
                        elements().map { it.name }.forEach(::add)
                    }
                }
            } catch (error: Throwable) {
                false to INVALID_JSON
            }
        }
    }

    private object PropFind : EventbusMessageHelper {
        val fileService = FileService()

        /**
         * ```json
         * {
         *     "status": int,
         *     "multiStatus": [{
         *          "href": string
         *     }]
         * }
         */
        suspend fun handle(request: Message<JsonObject>, fs: FileSystem) {
            val body = request.body()
            val depth = handleDepth(body[Labels.depth])
            val subject = body.get<String>(Constant.SUBJECT)
            val (exists, files) = fileService.viewFiles(subject, depth)

            buildXmlMessage(Labels.multiStatus) {
                it.state = 404
                it.data = Json.obj {
                    // todo
                }
            }.reply(request)
        }

        private fun handleDepth(depth: String?): Int {
            depth ?: return 0
            if (depth == "0")
                return 0
            if (depth == "1")
                return 1
            if (depth like "infinity")
                return 1
            return 0
        }

        private fun handlePropName(json: JsonObject) {
            TODO()
        }
    }

}