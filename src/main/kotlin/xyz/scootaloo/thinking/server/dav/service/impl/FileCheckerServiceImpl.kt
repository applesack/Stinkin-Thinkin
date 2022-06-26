package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import xyz.scootaloo.thinking.lang.Constant
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.lang.getLogger
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.domain.core.Pass
import xyz.scootaloo.thinking.server.dav.service.DetectorService
import xyz.scootaloo.thinking.server.dav.service.FileCheckerService
import xyz.scootaloo.thinking.server.dav.service.impl.util.DAVCommon
import java.util.concurrent.Future

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 20:13
 */
object FileCheckerServiceImpl : SingletonVertxService(), FileCheckerService {
    private val log by lazy { getLogger("file") }

    override val context = WebDAVContext.file

    override fun detect(path: String, ifExpr: String?): Future<Int> {
        TODO("Not yet implemented")
    }

    private object InternalProtocol {
        private const val prefix = "sys:dav:file"
        const val detect = "$prefix:detect"
    }

    private object Headers {
        const val ifExpr = "If"
    }

    override fun registerEventbusConsumer(contextName: String) {
        eb.consumer<JsonObject>(InternalProtocol.detect) {
            FileChecker.handleDetect(it)
        }

        log.info("eventbus 'FileChecker' ready; current context $contextName")
    }

    private object FileChecker : DAVCommon {
        private val detector = DetectorService()

        fun handleDetect(request: Message<JsonObject>) {
            val param = buildDetectParam(request.body())
            val state = detector.evaluate(param.subject, param.pass)
        }

        private fun buildDetectParam(form: JsonObject): Param {
            val subject = pathNormalize(form.getString(Constant.SUBJECT))
            val ifExpr = form.getString(Headers.ifExpr)
            return Param(subject, buildPass(ifExpr))
        }
    }

    private class Param(
        val subject: String,
        val pass: Pass?,
    )
}