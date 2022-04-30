package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/30 11:43
 */
public class JP908 {

    /**
     * 1, 2, 3, 4, 5, 6, 7, 8, 9; k = 3
     * avg = 5
     * 4, 5, 5, 5, 5, 5, 5, 5, 6
     */
    public int smallestRangeI(int[] nums, int k) {
        if (nums.length <= 1)
            return 0;

        int max = nums[0];
        int min = nums[0];
        for (int i = 1; i < nums.length; i++) {
            int num = nums[i];
            if (num > max) {
                max = num;
            } else if (num < min) {
                min = num;
            }
        }

        int avg = (max + min) / 2;
        min += Math.min(avg - min, k);
        max -= Math.min(max - avg, k);

        return max - min;
    }

    @Test
    public void test() {
        int[] input = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        System.out.println(smallestRangeI(input, 3));
    }
}
