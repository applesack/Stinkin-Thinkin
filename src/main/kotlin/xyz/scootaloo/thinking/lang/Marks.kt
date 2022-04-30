package xyz.scootaloo.thinking.lang

import kotlin.reflect.KClass

/**
 * @author flutterdash@qq.com
 * @since 2022/4/28 18:34
 */

annotation class Version(val ver: String)

annotation class Type(vararg val type: KClass<*>, val desc: String = "")

annotation class TestOnly