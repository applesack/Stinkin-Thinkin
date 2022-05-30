package xyz.scootaloo.thinking.lang

import kotlin.reflect.KClass

/**
 * @author flutterdash@qq.com
 * @since 2022/4/28 18:34
 */

@Retention(AnnotationRetention.SOURCE)
annotation class Version(val value: String)

@Retention(AnnotationRetention.SOURCE)
annotation class Type(vararg val type: KClass<*>, val desc: String = "")

@Retention(AnnotationRetention.SOURCE)
annotation class TestOnly

@Retention(AnnotationRetention.SOURCE)
annotation class Alias(val value: String)

@Retention(AnnotationRetention.SOURCE)
annotation class Context(val value: String)

@Retention(AnnotationRetention.SOURCE)
annotation class Immutable

@Retention(AnnotationRetention.SOURCE)
annotation class Stateless