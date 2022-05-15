package xyz.scootaloo.thinking.server.dav.domain.core

import io.vertx.core.file.FileProps
import xyz.scootaloo.thinking.lang.Alias
import xyz.scootaloo.thinking.server.dav.util.PathUtils

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 16:25
 */

object File {
    fun build(fullPath: String, basePath: String, author: String, props: FileProps): AFile {
        val file = newFile(fullPath, basePath, author, props)
        return if (props.isDirectory) {
            file.asDirectory()
        } else if (props.isRegularFile) {
            file.asRegularFile(props)
        } else {
            throw UnsupportedOperationException("the file '$fullPath' not support")
        }
    }

    private fun newFile(
        fullPath: String, basePath: String,
        author: String, props: FileProps,
    ): AFileAdapter {
        return AFileAdapter.of(fullPath, basePath, author, props)
    }

    private fun AFileAdapter.asRegularFile(props: FileProps): RegularFile {
        return RegularFile(this, props.size())
    }

    private fun AFileAdapter.asDirectory(): Directory {
        return Directory(this)
    }
}

sealed interface AFile {
    val href: String
    val displayName: String
    val author: String
    @Alias("getContentType") var type: String
    @Alias("creationDate") var creationDate: Long
    @Alias("getLastModified") var lastModified: Long
}

class AFileAdapter(
    override val href: String,
    override val displayName: String,
    override val author: String,
    override var creationDate: Long,
    override var lastModified: Long,
    override var type: String,
) : AFile {
    companion object {
        /**
         * fullPath: D:/abc/a.html
         * basePath: D:/abc
         * href: /a.html
         */
        fun of(fullPath: String, basePath: String, author: String, props: FileProps): AFileAdapter {
            val href = PathUtils.extractRelativePath(fullPath, basePath)
            return of(href, author, props)
        }

        private fun of(path: String, author: String, props: FileProps): AFileAdapter {
            val href = PathUtils.normalize(path)
            val display = PathUtils.mainName(href)
            return AFileAdapter(
                href = href,
                displayName = display,
                author = author,
                creationDate = props.creationTime(),
                lastModified = props.lastModifiedTime(),
                type = PathUtils.fileContentType(display)
            )
        }
    }
}

class RegularFile(adapter: AFileAdapter, val size: Long) : AFile by adapter

class Directory(adapter: AFileAdapter, override var type: String = "collection") : AFile by adapter