package xyz.scootaloo.thinking.util

import xyz.scootaloo.thinking.lang.currentTimeMillis
import xyz.scootaloo.thinking.lang.ifNotNull
import java.util.*
import kotlin.collections.HashMap

/**
 * ## 通用的超时回收器的功能实现
 *
 * 1. 将过期时间放置到TreeMap中不重复的位置
 * 2. 回收过期的键, 并用回调处理
 *
 * ### 超时回收期的数据结构
 *
 * 1. 一个存储键值对的映射表, 其中映射表的值可以指向2的键
 * 2. 一个存储过期时间的超时表, 数据结构是TreeMap, 键是Long, 即过期时间, 值是1中的键
 *
 * @author flutterdash@qq.com
 * @since 2022/5/29 23:50
 */
abstract class AbstractTimeoutRecycler<K : Any, V : Any> {

    private val timeoutTable = TreeMap<Long, K>()
    protected val valueTable = HashMap<K, Record<V>>()

    /**
     * ## 插入一个带超时信息的键值对
     *
     * 通过这个方法插入的键值对, 该键值对会在[datum]+[incremental]毫秒后被删除
     *
     * @param key 键
     * @param value 值
     * @param incremental 过期时间(从[datum]开始计算); 单位默认毫秒
     * @param datum 基准值, [datum]+[incremental]最终得到实际的过期时间; 单位默认毫秒
     */
    fun putTimeoutKeyValuePair(
        key: K, value: V, incremental: Long, datum: Long = currentTimeMillis(),
    ) {
        if (key in valueTable) {
            deleteKey(key)
        }

        val realExpiryTime = placeExpiryKeyWithoutRepetition(key, incremental, datum)
        valueTable[key] = Record(value, realExpiryTime)
    }

    /**
     * ## 插入一个永久键值对
     *
     * 通过这个方法插入的键值对永远不会被[doRecycle]调用回收
     */
    fun putKeyValuePair(key: K, value: V) {
        valueTable[key] = Record(value, 0)
    }

    /**
     * ## 将一个键放置到[timeoutTable]中
     *
     * 向[timeoutTable]中放置一个键的过期信息;
     * 如果[incremental]与已经存在的键重叠, 不会覆盖已经存在的键,
     * 而是以这个时间为原点, 向这个时间点的未来或者过去中找一个最近的没有重复的位置存放, 然后把这个位置返回;
     *
     * @param key 要存储的键
     * @param incremental [key]将在[datum]的[incremental]毫秒后过期
     * @param datum 基准时间, 默认为当前时间
     *
     * @return
     * 实际的过期时间
     */
    private fun placeExpiryKeyWithoutRepetition(
        key: K, incremental: Long, datum: Long = currentTimeMillis(),
    ): Long {
        val realExpiryTime = datum + incremental
        if (realExpiryTime !in timeoutTable) {
            timeoutTable[realExpiryTime] = key
            return realExpiryTime
        }

        var validExpiryTimePst = realExpiryTime - 1
        var validExpiryTimeFut = realExpiryTime + 1

        while (true) {
            if (validExpiryTimePst !in timeoutTable) {
                timeoutTable[validExpiryTimePst] = key
                return validExpiryTimePst
            } else {
                validExpiryTimePst--
            }

            if (validExpiryTimeFut !in timeoutTable) {
                timeoutTable[validExpiryTimeFut] = key
                return validExpiryTimeFut
            } else {
                validExpiryTimeFut++
            }
        }
    }

    /**
     * ## 回收所有失效的键值对(把这些键值对删除)
     *
     * - 失效的键值对: 指过期时间早于[currentTime]的记录;
     * - 当有键值对被删除时, 可以通过[callback]来监听删除动作, 当[callback]被调用时, 此键值对已经被删除;
     *
     * @param currentTime 指定一个失效的时间点, 默认为当前时间
     * @param callback 用于监听键值对的删除动作
     * @return
     * 返回此次回收有多少键值对被删除
     */
    fun doRecycle(
        currentTime: Long = currentTimeMillis(), callback: (Pair<K, V>) -> Unit,
    ): Int {
        if (timeoutTable.isEmpty() || valueTable.isEmpty()) {
            return 0
        }

        val invalidKeys = LinkedList<K>()
        for ((expiryTime, key) in timeoutTable) {
            if (expiryTime > currentTime) {
                invalidKeys.add(key)
            }
        }

        if (invalidKeys.isNotEmpty()) {
            for (key in invalidKeys) {
                deleteKey(key).ifNotNull {
                    callback(it)
                }
            }
        }

        return invalidKeys.size
    }

    /**
     * ## 刷新一个键值对的过期时间
     *
     * 处理逻辑非常简单, 如果此键已经不存在, 则不做任何处理;
     * 如果存在, 则先将其从系统中删除, 然后重新使用新的过期时间插入;
     */
    fun refreshKeyTimeoutInfo(
        key: K, incremental: Long, datum: Long = currentTimeMillis(),
    ) {
        deleteKey(key).ifNotNull { (_, v) ->
            putTimeoutKeyValuePair(key, v, incremental, datum)
        }
    }

    /**
     * ## 删除一条键值对
     *
     * @param key 要删除的键
     * @return
     * 返回被删除的项; 如果此键存在, 则返回此键值对, 如果不存在则返回null
     */
    fun deleteKey(key: K): Pair<K, V>? {
        val record = valueTable[key] ?: return null
        val timeoutInfo = record.timeoutInfo
        timeoutTable.remove(timeoutInfo)
        valueTable.remove(key)
        return key to record.value
    }

    class Record<V>(
        val value: V,
        val timeoutInfo: Long,
    )

}