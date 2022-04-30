package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/26 11:24
 */
public class JP883v2 {
    /**
     * 1, 2, 3
     * 3, 4, 5
     * 2, 4, 9
     *
     *
     */
    public int projectionArea(int[][] grid) {
        int top = 0;
        int side = 0;
        int row = grid.length;
        int col = grid[0].length;
        for (int r = 0; r < row; r++) {
            int max = 0;
            for (int c = 0; c<col; c++) {
                int cur = grid[r][c];
                if (cur > 0) {
                    top++;
                }
                if (cur > max) {
                    max = cur;
                }
                if (r > 0 && grid[r - 1][c] > cur) {
                    grid[r][c] = grid[r - 1][c];
                }
            }
            side += max;
        }

        int front = 0;
        for (int c = 0; c<col; c++) {
            front += grid[row - 1][c];
        }

        return top + front + side;
    }

    @Test
    public void test() {
        int[][] input = {
                {1, 2},
                {3, 4}
        };
        System.out.println(projectionArea(input));
    }
}
