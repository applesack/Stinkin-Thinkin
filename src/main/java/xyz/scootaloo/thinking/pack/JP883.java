package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/26 11:08
 */
public class JP883 {

    /**
     * [1, 2]
     * [3, 4]
     *
     * 7 + 4 + 6
     *
     * @param grid ignore
     * @return ignore
     */
    public int projectionArea(int[][] grid) {
        int top = 0;
        int right = 0;
        int[] dp = new int[grid[0].length];
        for (int[] nums : grid) {
            int rightMax = 0;
            for (int c = 0; c < nums.length; c++) {
                int cur = nums[c];
                if (cur > 0) {
                    top++;
                }
                if (cur > rightMax) {
                    rightMax = cur;
                }
                if (cur > dp[c]) {
                    dp[c] = cur;
                }
            }
            right += rightMax;
        }

        int sum = 0;
        for (int j : dp) {
            sum += j;
        }

        return top + sum + right;
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
