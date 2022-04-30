package xyz.scootaloo.thinking.struct.dav

import io.vertx.core.json.JsonObject

/**
 * @author flutterdash@qq.com
 * @since 2022/4/24 11:04
 */

sealed class LockRecord {
    abstract fun jsonify(): JsonObject
    fun isShared(): Boolean = this is SharedLockRecord
}

object InvalidLockRecord : LockRecord() {
    override fun jsonify(): JsonObject {
        throw IllegalAccessException()
    }
}

class ExclusiveLockRecord(
    val owner: String
) : LockRecord() {
    override fun jsonify(): JsonObject {
        TODO("Not yet implemented")
    }
}

class SharedLockRecord(
    val holders: Map<String, String> = HashMap()
) : LockRecord() {
    fun addHolder(name: String): String {
        return ""
    }

    fun removeHolder(name: String) {
    }

    override fun jsonify(): JsonObject {
        TODO("Not yet implemented")
    }
}