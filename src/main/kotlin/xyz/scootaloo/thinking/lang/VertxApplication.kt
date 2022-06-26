@file:Suppress("unused")

package xyz.scootaloo.thinking.lang

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.obj
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.slf4j.Logger
import xyz.scootaloo.thinking.server.component.DigestAuthService
import xyz.scootaloo.thinking.server.component.NoneAuthService

/**
 * @author flutterdash@qq.com
 * @since 2022/5/5 16:10
 */

typealias CoroutineBlock = suspend CoroutineScope.() -> Unit
typealias CoroutineEntrance = (CoroutineBlock) -> Job

typealias CoroutineReqHandler = suspend CoroutineScope.(RoutingContext) -> Unit

typealias AuthMode = VerificationMode

enum class VerificationMode {
    NONE, JWT, DIGEST
}

abstract class VertxHttpApplication(private val entrance: CoroutineEntrance) {

    abstract val log: Logger

    open val mountPoint: String = "/*"

    open val auth: AuthMode = AuthMode.NONE

    abstract fun config(router: Router)

    protected fun Router.get(path: String = "/*", handler: CoroutineReqHandler) =
        get(path).corHandler(handler)

    protected fun Router.options(path: String = "/*", handler: CoroutineReqHandler) =
        options(path).corHandler(handler)

    protected fun Router.put(path: String = "/*", handler: CoroutineReqHandler) =
        put(path).corHandler(handler)

    protected fun Router.post(path: String = "/*", handler: CoroutineReqHandler) =
        post(path).corHandler(handler)

    protected fun Router.delete(path: String = "/*", handler: CoroutineReqHandler) =
        delete(path).corHandler(handler)

    protected fun Router.trace(path: String = "/*", handler: CoroutineReqHandler) =
        trace(path).corHandler(handler)

    protected fun Router.connect(path: String = "/*", handler: CoroutineReqHandler) =
        connect(path).corHandler(handler)

    protected fun Router.patch(path: String = "/*", handler: CoroutineReqHandler) =
        patch(path).corHandler(handler)

    protected fun Router.propFind(path: String = "/*", handler: CoroutineReqHandler) =
        route(HttpMethod.PROPFIND, path).corHandler(handler)

    protected fun Router.propPatch(path: String = "/*", handler: CoroutineReqHandler) =
        route(HttpMethod.PROPPATCH, path).corHandler(handler)

    protected fun Router.mkcol(path: String = "/*", handler: CoroutineReqHandler) =
        route(HttpMethod.MKCOL, path).corHandler(handler)

    protected fun Router.head(path: String = "/*", handler: CoroutineReqHandler) =
        route(HttpMethod.HEAD, path).corHandler(handler)

    protected fun Router.copy(path: String = "/*", handler: CoroutineReqHandler) =
        route(HttpMethod.COPY, path).corHandler(handler)

    protected fun Router.move(path: String = "/*", handler: CoroutineReqHandler) =
        route(HttpMethod.MOVE, path).corHandler(handler)

    protected fun Router.lock(path: String = "/*", handler: CoroutineReqHandler) =
        route(HttpMethod.LOCK, path).corHandler(handler)

    protected fun Router.unlock(path: String = "/*", handler: CoroutineReqHandler) =
        route(HttpMethod.UNLOCK, path).corHandler(handler)

    private fun Route.corHandler(handler: CoroutineReqHandler) = Companion.corHandler(
        entrance, this, log, handler
    )

    abstract class InstanceFactory(
        private val lazy: (CoroutineEntrance) -> VertxHttpApplication,
    ) : Factory<CoroutineEntrance, VertxHttpApplication> {
        override fun invoke(input: CoroutineEntrance): VertxHttpApplication {
            return lazy(input)
        }
    }

    companion object {
        fun corHandler(
            entrance: CoroutineEntrance, route: Route,
            logger: Logger, handler: CoroutineReqHandler,
        ) {
            route.handler { ctx ->
                entrance {
                    try {
                        handler(ctx)
                    } catch (error: Throwable) {
                        logger.error("route process error", error)
                    }
                }
            }
        }
    }
}

class VertxHttpAppAssemblyFactory(
    private val registerCenter: VertxServiceRegisterCenter,
    private val provider: UserProvider,
    private val vertx: Vertx = registerCenter.vertx,
    private val entrance: CoroutineEntrance = registerCenter::startCoroutine,
) {

    fun assembles(factories: List<VertxHttpApplication.InstanceFactory>): Router {
        val rootRouter = Router.router(vertx)
        val log = registerCenter.log
        rootRouter.route().handler(bodyHandler())
        rootRouter.route().handler {
            preHandler(log, it)
            it.next()
        }

        val apps = factories.map { it(entrance) }
        for (app in apps) {
            val subRouter = Router.router(vertx)
            safeDoConfig(log, app, rootRouter, subRouter)
        }

        return rootRouter
    }

    private fun preHandler(log: Logger, ctx: RoutingContext) {
        val request = ctx.request()
        val remote = request.remoteAddress().hostAddress()
        val method = request.method().name()
        val uri = request.uri()
        log.info("$remote $method $uri")
    }

    private fun safeDoConfig(
        log: Logger, app: VertxHttpApplication, root: Router, child: Router,
    ) = try {
        child.mountAuthenticator(app.auth)
        app.config(child)
        root.route(app.mountPoint).subRouter(child)
    } catch (error: Throwable) {
        log.error("mount router error", error)
    }

    private fun Router.mountAuthenticator(mode: AuthMode) {
        val authenticator = when (mode) {
            AuthMode.DIGEST -> {
                DigestAuthService().apply { setProvider(provider) }
            }
            AuthMode.JWT -> {
                NoneAuthService
            }
            AuthMode.NONE -> {
                NoneAuthService
            }
        }
        if (authenticator != NoneAuthService) {
            VertxHttpApplication.corHandler(entrance, route(), authenticator.log)
            {
                authenticator.handle(it)
            }
        }
    }

    private fun bodyHandler(): BodyHandler {
        return BodyHandler.create().apply {
            this.setDeleteUploadedFilesOnEnd(true)
            this.setMergeFormAttributes(true)
        }
    }

}

interface Authenticator {

    val log: Logger

    suspend fun handle(ctx: RoutingContext)

    fun putUserInContext(ctx: RoutingContext, user: User) {
        ctx.setUser(io.vertx.ext.auth.User.create(user.jsonify()))
    }

}

interface UserProvider {
    fun findByName(username: String): Future<User?>
}

interface User {
    val id: Int
    val username: String
    val password: String

    fun jsonify(): JsonObject {
        return Json.obj {
            this[Constant.ID] = id
            this[Constant.USERNAME] = username
            this[Constant.PASSWORD] = password
        }
    }
}