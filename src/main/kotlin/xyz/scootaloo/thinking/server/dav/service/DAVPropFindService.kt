package xyz.scootaloo.thinking.server.dav.service

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import org.dom4j.Document
import org.dom4j.Element
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.component.CacheService
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.util.DAVCommonLabels
import xyz.scootaloo.thinking.server.dav.util.JsonToXml
import xyz.scootaloo.thinking.server.dav.util.XmlHelper

/**
 * @author flutterdash@qq.com
 * @since 2022/5/8 11:31
 */
interface DAVPropFindService : VertxService {

    suspend fun handle(ctx: RoutingContext)

    companion object : VertxService.SingletonFactory<DAVPropFindService>(Impl)

    // ------------------------------ Implements ---------------------------------

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val propFind = "$prefix:propFind"
    }

    private object Impl : SingletonVertxService(), DAVPropFindService, HttpApplicationHelper {
        val log by lazy { getLogger("propFind") }

        override var context = WebDAVContext.file
        override suspend fun handle(ctx: RoutingContext) {
            val arguments = resolveRequestArguments(ctx)
            val result = eb.callService(InternalProtocol.propFind, arguments).await().body()
            resolveResponse(ctx, result)
        }

        override fun registerEventbusConsumer(contextName: String) {
            eb.coroutineConsumer<JsonObject>(InternalProtocol.propFind) { request ->
                val result = PropFind.handle(request.body())
                request.reply(result)
            }

            log.info("eventbus PropFind service ready; current context: $contextName")
        }

        private suspend fun resolveRequestArguments(ctx: RoutingContext): JsonObject {
            return waitBlocking { Resolver.resolveRequestBody(ctx) }
        }

        private suspend fun resolveResponse(ctx: RoutingContext, json: JsonObject) {
            val status = json.getInteger(Constant.STATUS)
            if (status != 0) {
                ctx.fail(status)
                return
            } else {
                val xml = waitBlocking {
                    JsonToXml.convert(json.getJsonObject(Labels.propFind), Labels.multiStatus)
                }
                ctx.replyWithXml(xml)
            }
        }
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

    private object PropFind {
        /**
         * ```json
         * {
         *     "status": int,
         *     "multiStatus": [{
         *          "href": string
         *     }]
         * }
         */
        suspend fun handle(request: JsonObject): JsonObject {
            return Json.obj {
                this[Constant.STATUS] = 0
                this[Labels.response] = Json.array {
                    request.getValue(Labels.propName).ifNotNull {

                    }
                }
            }
        }

        private fun handleDepth(depth: String?): Int {
            depth ?: return 0
            TODO()
        }

        private fun handlePropName(): JsonObject {
            TODO()
        }
    }
}