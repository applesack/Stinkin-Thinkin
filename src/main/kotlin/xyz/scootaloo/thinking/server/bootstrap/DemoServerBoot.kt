package xyz.scootaloo.thinking.server.bootstrap

import xyz.scootaloo.thinking.server.demo.DemoServer

/**
 * @author flutterdash@qq.com
 * @since 2022/5/6 19:10
 */
fun main() {
    System.setProperty("log4j.skipJansi", "false")
    DemoServer.bootstrap()
}