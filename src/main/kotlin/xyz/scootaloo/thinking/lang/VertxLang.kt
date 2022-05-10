package xyz.scootaloo.thinking.lang

import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.await

/**
 * @author flutterdash@qq.com
 * @since 2022/5/2 23:48
 */

typealias CodeBlock<T> = () -> T
typealias CommandBlock = () -> Unit
typealias PromiseBlock<T> = (Promise<T>) -> Unit

infix fun <T> Promise<T>.complete(value: T?) {
    this.complete(value)
}

inline fun <T, R> Future<T>.trans(crossinline lazy: (T) -> R): Future<R> {
    return this.transform { done ->
        if (done.succeeded()) {
            Future.succeededFuture(lazy(done.result()))
        } else {
            Future.failedFuture(done.cause())
        }
    }
}

@JvmName("normalBlocking")
fun VertxService.blocking(block: CommandBlock): Future<Unit> {
    return blocking<Unit>(block)
}

@JvmName("typedBlocking")
fun <T> VertxService.blocking(block: CodeBlock<T>): Future<T> {
    return vertx.executeBlocking {
        it complete block()
    }
}

suspend fun <T> VertxService.waitBlocking(block: CodeBlock<T>): T {
    return vertx.executeBlocking<T> {
        it.complete(block())
    }.await()
}