package xyz.scootaloo.thinking.server.dav.util

import xyz.scootaloo.thinking.server.dav.domain.core.AFile
import xyz.scootaloo.thinking.server.dav.domain.core.State

/**
 * @author flutterdash@qq.com
 * @since 2022/5/27 13:13
 */
interface MultipleIterator {

    fun goto(path: String): Pair<State, AFile>

    fun hasNext(): Boolean

}