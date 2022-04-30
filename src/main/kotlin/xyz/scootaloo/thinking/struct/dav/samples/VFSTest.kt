package xyz.scootaloo.thinking.struct.dav.samples

import xyz.scootaloo.thinking.struct.dav.VirtualFileSystem

/**
 * @author flutterdash@qq.com
 * @since 2022/4/24 13:51
 */
object VFSTest {
    @JvmStatic
    fun main(args: Array<String>) {
        VirtualFileSystem.struct.createDirectory("main/java/xyz/server")
        VirtualFileSystem.struct.createDirectory("main/kotlin/xyz/server")
        VirtualFileSystem.struct.createDirectory("main/kotlin/xyz/server\\Main.kt")
        VirtualFileSystem.struct.createDirectory("main/kotlin/xyz/server\\Test.kt")
        VirtualFileSystem.struct.createDirectory("main/java/xyz/server\\Test.kt")
        VirtualFileSystem.struct.createDirectory("main/resource")
        println()
        VirtualFileSystem.struct.deleteDirectory("main/kotlin/xyz/server/Test.kt")
        println()
    }
}