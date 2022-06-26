package xyz.scootaloo.thinking.server.dav.service.impl

import io.vertx.ext.web.RoutingContext
import xyz.scootaloo.thinking.lang.SingletonVertxService
import xyz.scootaloo.thinking.server.dav.application.WebDAVContext
import xyz.scootaloo.thinking.server.dav.service.DAVOptionsService

/**
 * @author flutterdash@qq.com
 * @since 2022/6/7 13:53
 */
object OptionsImpl : SingletonVertxService(), DAVOptionsService {
    override val context = WebDAVContext.httpServer

    override fun handle(ctx: RoutingContext) {
        val headers = ctx.response()
        headers.putHeader(Term.dav, "1,2")
        headers.putHeader(Term.allow, public)
        headers.putHeader(Term.msAuthorVia, Term.dav)
        headers.putHeader(Term.public, public)
        ctx.end()
    }

    private const val public = "OPTIONS,TRACE,GET,HEAD,DELETE,PUT,POST,COPY,MOVE,MKCOL,PROPFIND,PROPPATCH,LOCK,UNLOCK"

    private object Term {
        const val dav = "DAV"
        const val allow = "Allow"
        const val msAuthorVia = "MS-Author-Via"
        const val public = "Public"
    }

}