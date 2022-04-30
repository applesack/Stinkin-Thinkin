package xyz.scootaloo.thinking.pack

import org.junit.jupiter.api.Test
import java.util.LinkedList

/**
 * @author flutterdash@qq.com
 * @since 2022/4/27 12:27
 */
class KP417 {

    private fun pacificAtlantic(heights: Array<IntArray>): List<List<Int>> {
        val row = heights.size
        val col = heights[0].size
        val pac = Array(row) { BooleanArray(col) }
        val atl = Array(row) { BooleanArray(col) }

        val direction = arrayOf(
            intArrayOf(-1, 0),
            intArrayOf(1, 0),
            intArrayOf(0, 1),
            intArrayOf(0, -1)
        )

        val que = LinkedList<Pair<Int, Int>>()
        que.add(0 to 0)
        while (que.isNotEmpty()) {
            val (r, c) = que.removeFirst()
            val cur = heights[r][c]
            pac[r][c] = true
            for ((l, t) in direction) {
                val nextR: Int = (r + t)
                val nextC: Int = (c + l)
                if ((nextR in 0 until row) && (nextC in 0 until col)) {
                    if (pac[nextR][nextC])
                        continue
                    if (nextC == 0 || nextR == 0) {
                        pac[nextR][nextC] = true
                        que.addLast(nextR to nextC)
                        continue
                    }
                    if (heights[nextR][nextC] >= cur) {
                        pac[nextR][nextC] = true
                        que.addLast(nextR to nextC)
                    }
                }
            }
        }

        que.clear()
        que.addLast(row - 1 to col - 1)
        while (que.isNotEmpty()) {
            val (r, c) = que.removeFirst()
            val cur = heights[r][c]
            atl[r][c] = true
            for ((l, t) in direction) {
                val nextR = r + t
                val nextC = c + l
                if (nextR in 0 until row && nextC in 0 until col) {
                    if (atl[nextR][nextC])
                        continue
                    if (nextC == col - 1 || nextR == row - 1) {
                        atl[nextR][nextC] = true
                        que.addLast(nextR to nextC)
                        continue
                    }
                    if (heights[nextR][nextC] >= cur) {
                        atl[nextR][nextC] = true
                        que.addLast(nextR to nextC)
                    }
                }
            }
        }

        val res = ArrayList<ArrayList<Int>>()
        for (r in 0 until row) {
            for (c in 0 until col) {
                if (pac[r][c] && atl[r][c]) {
                    res.add(arrayListOf(r, c))
                }
            }
        }

        return res
    }

    @Test
    fun test() {
        val input = arrayOf(
            intArrayOf(1, 2, 2, 3, 5),
            intArrayOf(3, 2, 3, 4, 4),
            intArrayOf(2, 4, 5, 3, 1),
            intArrayOf(6, 7, 1, 4, 5),
            intArrayOf(5, 1, 1, 2, 4),
        )

        println(pacificAtlantic(input))
    }
}