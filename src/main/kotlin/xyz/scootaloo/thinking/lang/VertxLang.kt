package xyz.scootaloo.thinking.lang

import io.vertx.core.Future
import io.vertx.core.Promise

/**
 * @author flutterdash@qq.com
 * @since 2022/5/2 23:48
 */

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