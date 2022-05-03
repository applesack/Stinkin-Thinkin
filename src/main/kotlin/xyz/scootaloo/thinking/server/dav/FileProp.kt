package xyz.scootaloo.thinking.server.dav

/**
 * @author flutterdash@qq.com
 * @since 2022/4/24 10:46
 */

class FilePropBlock(
    val file: String,
    val entity: FileEntityProp,
    val extra: FileExtraProp
)

class FileEntityProp(
    val file: String,
    val size: Long,
    val created: Long,
    val updated: Long
)

class FileExtraProp(
    val uploader: String,
    val level: Int
)