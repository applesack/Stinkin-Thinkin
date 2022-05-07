package xyz.scootaloo.thinking.server.bootstrap

import xyz.scootaloo.thinking.server.dav.WebDAVServer

/**
 * @author flutterdash@qq.com
 * @since 2022/5/3 16:14
 */
fun main() {
    System.setProperty("log4j.skipJansi", "false")
    WebDAVServer.bootstrap()
}