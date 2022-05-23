package xyz.scootaloo.thinking.server.component.impl

import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import xyz.scootaloo.thinking.lang.*
import xyz.scootaloo.thinking.server.component.DigestAuthService
import xyz.scootaloo.thinking.util.Convert

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 20:32
 */
object DigestAuthServiceImpl : SingletonVertxService(), DigestAuthService {
    override val log by lazy { getLogger("auth:digest") }
    override val context = ContextRegedit.httpServer
    private lateinit var provider: UserProvider

    override fun setProvider(provider: UserProvider) {
        this.provider = provider
    }

    override suspend fun handle(ctx: RoutingContext) {
        val authorization = ctx.request().headers()[Term.H_AUTHORIZATION] ?: return challenge(ctx)
        verify(ctx, authorization)
    }

    private suspend fun verify(ctx: RoutingContext, authorization: String) {
        val method = ctx.method()
        val (valid, authHeader) = Helper.parseAuthHeader(authorization, method)
        if (!valid) {
            challenge(ctx)
        } else {
            verifyCore(ctx, authHeader())
        }
    }

    private suspend fun verifyCore(ctx: RoutingContext, authHeader: AuthorizationHeader) {
        val (valid, expired) = Helper.validateNonce(authHeader.nonce)
        if (!valid) {
            return challenge(ctx)
        }
        if (expired) {
            return challenge(ctx, true)
        }
        val user = provider.findByName(authHeader.username).await() ?: return challenge(ctx)
        val computedResponse = Helper.computedResponse(authHeader, user.password)
        if (computedResponse == authHeader.response) {
            verified(ctx, authHeader, user)
        } else {
            challenge(ctx)
        }
    }

    private fun verified(ctx: RoutingContext, header: AuthorizationHeader, user: User) {
        ctx.response().putHeader(
            Term.H_AUTHENTICATION_INFO, Helper.authorizationInfo(header, user.password)
        )
        putUserInContext(ctx, user)
        ctx.next()
    }

    private fun challenge(ctx: RoutingContext, stale: Boolean = false) {
        val response = ctx.response()
        response.putHeader(Term.H_AUTHENTICATE, Helper.challenge(stale))
        response.statusCode = Term.S_UNAUTHORIZED
        response.end()
    }

    private fun RoutingContext.method(): String {
        return request().method().toString()
    }

    private object Helper {
        fun parseAuthHeader(
            authorization: String, method: String,
        ): Pair<Boolean, ValueHolder<AuthorizationHeader>> {
            if (authorization.startsWith(Term.DIGEST_PREFIX)) {
                val rest = authorization.substring(Term.DIGEST_PREFIX.length + 1)
                    .replace("\"", "")
                val result = parseAuthHeaderCore(rest, method)
                if (result != null) {
                    return true to ValueHolder(result)
                }
            }
            return false to ValueHolder.empty()
        }

        fun challenge(stale: Boolean = false): String {
            val parts = mutableListOf(
                Triple(Term.C_REALM, Config.DEF_REALM, true),
                Triple(Term.C_QOP, Config.DEF_QOP, true),
                Triple(Term.C_NONCE, newNonce(), true)
            )
            if (stale) {
                parts.add(Triple(Term.C_STALE, "true", false))
            }
            return "${Term.DIGEST_PREFIX} ${parts.format()}"
        }

        fun authorizationInfo(header: AuthorizationHeader, password: String): String {
            return listOf(
                Triple(Term.C_QOP, Config.DEF_QOP, true),
                Triple(Term.C_RSP_AUTH, rspAuth(header, password), true),
                Triple(Term.C_CLIENT_NONCE, header.clientNonce, true),
                Triple(Term.C_NONCE_COUNTER, header.nonceCounter, false)
            ).format()
        }

        private fun parseAuthHeaderCore(authorization: String, method: String): AuthorizationHeader? {
            val params = HashMap<String, String>()
            for (item in authorization.split(',')) {
                val idx = item.indexOf('=')
                if (idx == -1)
                    continue
                val key = item.substring(0, idx).trim()
                val value = item.substring(idx + 1)
                params[key] = value
            }

            return try {
                AuthorizationHeader(
                    username = params[Term.C_USER]
                        ?: params[Term.C_USERNAME]!!,
                    realm = params[Term.C_REALM]!!,
                    method = method,
                    nonce = params[Term.C_NONCE]!!,
                    uri = params[Term.C_URI]!!,
                    nonceCounter = params[Term.C_NONCE_COUNTER]!!,
                    clientNonce = params[Term.C_CLIENT_NONCE]!!,
                    response = params[Term.C_RESPONSE]!!,
                    qop = params[Term.C_QOP]!!
                )
            } catch (nullErr: NullPointerException) {
                null
            }
        }

        private fun rspAuth(header: AuthorizationHeader, password: String): String {
            val a1Hash = header.run { md5("$username:$realm:$password") }
            val a2Hash = header.run { md5(":$uri") }
            return header.run {
                md5("$a1Hash:$nonce:$nonceCounter:$clientNonce:$qop:$a2Hash")
            }
        }

        fun computedResponse(header: AuthorizationHeader, password: String): String {
            val a1Hash = header.run { md5("$username:$realm:$password") }
            val a2Hash = header.run { md5("$method:$uri") }
            return header.run {
                md5("$a1Hash:$nonce:$nonceCounter:$clientNonce:$qop:$a2Hash")
            }
        }

        // 检查密文是否被篡改: (valid, expired)
        fun validateNonce(nonce: String): Pair<Boolean, Boolean> = try {
            val plainNonce = Convert.base64decode(nonce).trim('\"')
            val timestamp = plainNonce.substring(0, plainNonce.indexOf(' '))
            if (nonce == newNonce(timestamp)) {
                if (currentTimeMillis() - timestamp.toLong() > (Config.MAX_NONCE_AGE_SECONDS * 1000)) {
                    true to true
                } else {
                    true to false
                }
            } else {
                false to false
            }
        } catch (e: Throwable) {
            false to false
        }

        // "timestamp md5(timestamp:private_key)"
        private fun newNonce(timestamp: String = currentTimeMillis().toString()): String {
            val secret = md5("$timestamp:${Config.DEF_PRIVATE_KEY}")
            return Convert.base64encode("\"$timestamp $secret\"")
        }

        private fun List<Triple<String, String, Boolean>>.format(): String {
            return joinToString(",") {
                if (it.third) {
                    "${it.first}=\"${it.second}\""
                } else {
                    "${it.first}=${it.second}"
                }
            }
        }

        private fun md5(data: String): String {
            return Convert.md5(data)
        }
    }

    private object Config {
        const val DEF_REALM = "vertx-web-dav"
        const val DEF_QOP = "auth"
        const val DEF_PRIVATE_KEY = "fly me to the moon"
        const val MAX_NONCE_AGE_SECONDS = 20
    }

    private object Term {
        const val H_AUTHENTICATE = "WWW-Authenticate" // 质询
        const val H_AUTHORIZATION = "Authorization"   // 响应
        const val H_AUTHENTICATION_INFO = "Authentication-Info"
        const val S_UNAUTHORIZED = 401

        const val DIGEST_PREFIX = "Digest"
        const val C_USER = "user"
        const val C_USERNAME = "username"
        const val C_QOP = "qop"
        const val C_RSP_AUTH = "rspauth"
        const val C_CLIENT_NONCE = "cnonce"
        const val C_RESPONSE = "response"
        const val C_NONCE_COUNTER = "nc"
        const val C_NONCE = "nonce"
        const val C_URI = "uri"
        const val C_REALM = "realm"
        const val C_STALE = "stale"
    }

    private class AuthorizationHeader(
        val username: String,
        val realm: String,
        val method: String,
        val uri: String,
        val nonce: String,
        val nonceCounter: String,
        val clientNonce: String,
        val qop: String,
        val response: String,
    )

}