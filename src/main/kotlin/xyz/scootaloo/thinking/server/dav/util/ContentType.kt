package xyz.scootaloo.thinking.server.dav.util

import io.vertx.core.Vertx
import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.VertxUtils
import java.util.*

/**
 * @author flutterdash@qq.com
 * @since 2022/5/16 17:41
 */
object ContentType : VertxUtils {
    private val mapper = HashMap<String, String>()

    fun of(postfix: String): String {
        val post = if (postfix.startsWith('.')) postfix.substring(1) else postfix
        return mapper[post] ?: bin()
    }

    fun xml(): String {
        return mapper["xml"]!!
    }

    fun html(): String {
        return mapper["html"]!!
    }

    fun json(): String {
        return mapper["json"]!!
    }

    fun bin(): String {
        return mapper["bin"]!!
    }

    suspend fun refreshData(fs: FileSystem) {
        val file = "content-type.txt"
        val fileContent = readFileAsString(fs, file)
        loadData(fileContent)
    }

    fun display() {
        for ((postfix, mime) in mapper) {
            println("$postfix = $mime")
        }
    }

    private suspend fun readFileAsString(fs: FileSystem, file: String): String {
        val buff = fs.readFile(file).await()
        return buff.getString(0, buff.length())
    }

    private fun loadData(text: String) {
        for (line in text.lines()) {
            if (' ' in line) {
                continue
            }
            val idx = line.indexOf('=')
            if (idx < 0)
                continue
            val mimeType = line.substring(0, idx)
            val postfixList = line.substring(idx + 1)
            for (postfix in postfixList.split(',')) {
                mapper[postfix] = mimeType
            }
        }
    }

}

private class CodeGen {

    @Test
    fun showData(): Unit = runBlocking {
        val vertx = Vertx.vertx()
        val fs = vertx.fileSystem()
        ContentType.refreshData(fs)
        ContentType.display()
    }

    @Test
    fun genData(): Unit = runBlocking {
        // https://www.freeformatter.com/mime-types-list.html

        val vertx = Vertx.vertx()
        val fs = vertx.fileSystem()
        try {
            val fileContent = readFile(fs)
            val meta = handleFile(fileContent)
            codeGen(meta)
            println("==================FINISH====================")
        } finally {
            vertx.close().await()
        }
    }

    private suspend fun readFile(fs: FileSystem): String {
        val file = "content-type-raw.txt"
        println("reading file $file")
        val exists = fs.exists(file).await()
        if (!exists) {
            println("file not exists, process exit")
            throw java.lang.RuntimeException()
        }
        val props = fs.props(file).await()
        println("file size ${props.size()}.")

        val buff = fs.readFile(file).await()
        return buff.getString(0, buff.length())
    }

    private fun handleFile(file: String): List<Pair<String, String>> {
        val result = LinkedList<Pair<String, String>>()

        var lineNumber = 1
        for (line in file.lines()) {
            lineNumber++
            if (line.isBlank())
                continue
            val items = line.split('\t')

            val postfix = items[2]
            val mimeType = items[1].trim(',', ' ')

            result.add(postfix to mimeType)
        }
        return result
    }

    private fun codeGen(list: List<Pair<String, String>>) {
        println("-------------------BEGIN--------------------")
        for ((postfix, mimeType) in list) {
            val realMimeType = mimeType.trim(' ', ',')
            val realPostfix = postfix.split(',')
                .filter { it.isNotBlank() }
                .joinToString(",") { it.trim(',', '.', ' ') }
            println("${realMimeType.lower()}=${realPostfix.lower()}")
        }
        println("-------------------END----------------------")
    }

    private fun String.lower(): String {
        return lowercase(Locale.getDefault())
    }

}