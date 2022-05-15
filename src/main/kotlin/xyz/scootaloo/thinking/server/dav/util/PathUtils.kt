package xyz.scootaloo.thinking.server.dav.util

/**
 * @author flutterdash@qq.com
 * @since 2022/5/15 11:00
 */
object PathUtils {

    fun mainName(path: String): String {
        val spIdx = path.lastIndexOf('/')
        return path.substring(spIdx + 1)
    }

    fun extractRelativePath(fullPath: String, basePath: String): String {
        if (basePath.isEmpty()) return normalize(fullPath)
        val last = basePath.last()
        val prefix = if (last == '/' || last == '\\')
            basePath.length - 1
        else
            basePath.length
        return normalize(fullPath.substring(prefix))
    }

    fun normalize(path: String): String {
        if (path.isEmpty())
            return "/"
        val idx = path.indexOf('\\')
        val head = path.first()
        if (idx < 0 && head == '/')
            return path
        val buffSize = if (head == '/' || head == '\\') path.length else path.length + 1
        val buff = StringBuilder(buffSize)
        if (buffSize != path.length) {
            buff.append('/')
        }
        for (ch in path) {
            if (ch == '\\')
                buff.append('/')
            else
                buff.append(ch)
        }
        return buff.toString()
    }

    fun fileContentType(mainName: String): String {
        TODO()
    }

}