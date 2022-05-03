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

suspend fun VertxService.waitBlocking(block: CommandBlock) {
    vertx.executeBlocking<Unit> {
        it complete block()
    }.await()
}

suspend fun <T> VertxService.waitBlocking(block: PromiseBlock<T>): T {
    return vertx.executeBlocking(block).await()
}