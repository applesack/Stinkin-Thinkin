package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.eventbus.Message
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import org.dom4j.Document
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.lib.HttpHeaderHelper
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.domain.core.Directory
import xyz.scootaloo.thinking.server.dav.domain.core.RegularFile
import xyz.scootaloo.thinking.server.dav.domain.core.State
import xyz.scootaloo.thinking.server.dav.service.DAVLockService
import xyz.scootaloo.thinking.server.dav.service.DAVPropFindService
import xyz.scootaloo.thinking.server.dav.service.impl.util.MultiStatus
import xyz.scootaloo.thinking.server.dav.service.fs.VirtualFileSystem
import xyz.scootaloo.thinking.server.dav.util.JsonToXml
import xyz.scootaloo.thinking.server.dav.util.XmlHelper
import xyz.scootaloo.thinking.struct.http.Depth
import xyz.scootaloo.thinking.struct.http.ResponseStore
import xyz.scootaloo.thinking.util.Convert
import xyz.scootaloo.thinking.util.DateHelper

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 20:41
 */
object PropFindImpl : SingletonVertxService(), DAVPropFindService, EventbusMessageHelper {

    val log by lazy { getLogger("PropFind") }

    override var context = WebDAVContext.file

    override suspend fun handle(ctx: RoutingContext) {
        val arguments = Resolver.resolveRequest(ctx)
        val result = eb.callService(InternalProtocol.propFind, arguments).await()
        ctx.smartReply(result.body())
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.coroutineConsumer<JsonObject>(InternalProtocol.propFind) {
            PropFind.handle(it, fs)
        }

        log.info("eventbus 'PropFind' service ready; current context: $contextName")
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav"
        const val propFind = "$prefix:propFind"
    }

    private object Resolver : XmlHelper {
        /**
         * ```json
         * {
         *     "subject": string,
         *     "depth": string,
         *     "allProp": bool?,
         *     "props": [string]?
         * }
         * ```
         * 如果请求头中包含depth属性, 则使用此depth属性, 否则, 视为客户端提交了depth为infinity的请求;
         * 如果请求不包含请求体, 或者请求体格式有误(例如不是xml格式), 则默认启用allProp;
         * 如果xml请求体中带有prop属性, 则响应体中只会返回服务器能处理并且prop中含有的属性;
         * 如果xml请求体中带有prop属性, 则忽略之前计算得到的allProp值, 将这个值重置为false;
         */
        suspend fun resolveRequest(ctx: RoutingContext): JsonObject {
            return awaitParallelBlocking block@{
                val result = Json.obj { resolveDefault(ctx) }
                val xmlBody = ctx.body().asString() ?: return@block result
                val document = safeParseXml(log, xmlBody) ?: return@block result
                result.resolveXmlBody(document)
            }
        }

        private fun JsonObject.resolveDefault(ctx: RoutingContext): JsonObject {
            this[Constant.SUBJECT] = Convert.decodeUriComponent(ctx.pathParam("*") ?: "/")
            this[Headers.depth] = ctx.request().headers()[Headers.depth]
            this[Labels.allProp] = true
            return this
        }

        private fun JsonObject.resolveXmlBody(document: Document): JsonObject {
            val root = document.rootElement
            val props = root.first(Labels.prop)
            this[Labels.allProp] = root.hasChild(Labels.allProp)
            props.ifNotNull {
                val propArray = JsonArray()
                it.collectChildTags().forEach(propArray::add)
                if (!propArray.isEmpty) {
                    this[Labels.allProp] = null
                    this[Labels.props] = propArray
                }
            }
            return this
        }
    }

    private object Labels {
        const val href = "href"
        const val multiStatus = "multiStatus"
        const val prop = "prop"
        const val status = "status"
        const val allProp = "allProp"
        const val props = "props"
        const val propStat = "propStat"
        const val response = "response"
    }

    private object Headers {
        const val depth = "Depth"
    }

    private object PropFind : EventbusMessageHelper, HttpHeaderHelper {

        private val lockService = DAVLockService()

        /**
         * ```json
         * {
         *     "status": int,
         *     "multiStatus": [{
         *          "href": string
         *     }]
         * }
         * ```
         */
        suspend fun handle(request: Message<JsonObject>, fs: FileSystem) {
            val param = buildParam(request.body())
            val files = VirtualFileSystem.viewFiles(
                param.subject, param.depth.depth, param.depth.noRoot, fs
            )

            if (files.isEmpty()) {
                return buildHtmlMessage {
                    it.state = Status.notFound
                    it.data = ResponseStore.fileNotFount
                }.reply(request)
            }

            buildXmlMessage(Labels.multiStatus) scope@{
                val json = JsonObject()
                if (files.isEmpty()) {
                    return@scope
                }

                val multiResponse = JsonArray()
                for ((state, file) in files) {
                    if (state == State.PASS) {
                        multiResponse.add(buildResponse(param, file))
                    } else {
                        TODO()
                    }
                }

                if (!multiResponse.isEmpty) {
                    json[Labels.response] = multiResponse
                }

                it.state = Status.multiStatus
                it.data = json
            }.reply(request)
        }

        private fun buildResponse(param: Param, file: AFile): JsonObject {
            return Json.obj {
                this[Labels.href] = Convert.encodeUriComponent(file.href)
                this[Labels.propStat] = Json.obj {
                    this[Labels.prop] = buildProp(param, file)
                    this[Labels.status] = MultiStatus.statusOf(Status.ok)
                }
            }
        }

        private const val creationDate = "CreationDate"
        private const val lastModified = "getLastModified"
        private const val displayName = "DisplayName"
        private const val contentLength = "getContentLength"
        private const val resourceType = "ResourceType"
        private const val supportedLock = "SupportedLock"
        private const val author = "author"
        private const val dateFormatMark = "ns0:dt"

        private fun buildProp(param: Param, file: AFile): JsonObject {
            val root = JsonObject()

            if (param.allProp || contains(creationDate, param.props)) {
                val fileCreationDate = DateHelper.formatDateTimeTZ(file.creationDate)
                root[creationDate] = JsonToXml.textTag(
                    fileCreationDate, dateFormatMark to "dateTime.tz"
                )
            }

            if (param.allProp || contains(lastModified, param.props)) {
                val filLastModified = DateHelper.formatRFC1123(file.lastModified)
                root[lastModified] = JsonToXml.textTag(
                    filLastModified,
                    dateFormatMark to "dateTime.rfc1123"
                )
            }

            if (param.allProp || contains(displayName, param.props)) {
                root[displayName] = Convert.encodeUriComponent(file.displayName)
            }

            if ((param.allProp || contains(contentLength, param.props)) && file is RegularFile) {
                root[contentLength] = file.size
            }

            if (param.allProp || contains(resourceType, param.props)) {
                root[resourceType] = if (file is Directory) {
                    Json.obj {
                        this[file.type] = JsonToXml.closedTag()
                    }
                } else {
                    file.type
                }
            }

            if (param.allProp || contains(author, param.props)) {
                root[author] = file.author
            }

            if (param.allProp || contains(supportedLock, param.props)) {
                root[supportedLock] = lockService.displaySupportedLock()
            }

            return root
        }

        private fun buildParam(form: JsonObject): Param {
            return Param(
                subject = form[Constant.SUBJECT],
                depth = parseDepthHeader(form[Headers.depth] ?: "infinity"),
                allProp = Labels.allProp in form,
                props = wrapInSet(form.getJsonArray(Labels.props))
            )
        }

        private fun wrapInSet(array: JsonArray?): Set<String>? {
            return array?.map { it.toString().lowercase() }?.toSet()
        }

        private fun contains(key: String, set: Set<String>?): Boolean {
            set ?: return false
            return key.lowercase() in set
        }
    }

    private object Status {
        const val ok = 200
        const val notFound = 404
        const val multiStatus = 207
    }

    private class Param(
        val subject: String,
        val depth: Depth,
        val allProp: Boolean,
        val props: Set<String>?,
    )

}

private class PropFindUnitTest : TestDsl {

    @Test
    fun testXmlConvert() {
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
                        "creationDate": {
                            "-ns0:dt": "dateTime.rfc1123",
                            "#text": "1997-12-01T17:42:21-08:00"
                        },
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

}