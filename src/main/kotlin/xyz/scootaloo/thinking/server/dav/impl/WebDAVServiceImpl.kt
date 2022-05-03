package xyz.scootaloo.thinking.server.dav.impl

import io.vertx.core.MultiMap
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.lib.HttpHeaderHelper
import xyz.scootaloo.thinking.server.dav.DAVTopLabels
import xyz.scootaloo.thinking.server.dav.DAVXmlHelper
import xyz.scootaloo.thinking.server.dav.WebDAVService
import xyz.scootaloo.thinking.struct.http.CommonHeader

/**
 * @author flutterdash@qq.com
 * @since 2022/5/2 23:10
 */
object WebDAVServiceImpl : SingletonVertxService(), WebDAVService, HttpHeaderHelper, DAVXmlHelper {

    override suspend fun lock(ctx: RoutingContext) {
        val body = waitBlocking<JsonObject> {
            it complete Json.obj {
                InputHelper.prepareLockOptionHeader(ctx.request().headers(), this)
                InputHelper.prepareLockOptionBody(ctx.bodyAsString, this)
            }
        }
        val message = eb.callService(Protocol.lock, body).await()
        TODO("Not yet implemented")
    }

    override suspend fun unlock() {
        TODO("Not yet implemented")
    }

    object InputHelper {
        fun prepareLockOptionHeader(header: MultiMap, container: JsonObject) {
            header[CommonHeader.timeout].ifNotNull { timeout ->
                parseTimeoutHeader(timeout).ifValid {
                    container[CommonHeader.timeout] = it
                }
            }
        }

        fun prepareLockOptionBody(xml: String, container: JsonObject) {
            parseLockInfo(xml).ifValid {
                container[DAVTopLabels.lockInfo] = it
            }
        }
    }

    private object Protocol {
        private const val prefix = "eb:dav:"
        const val lock = "$prefix:lock"
    }



}