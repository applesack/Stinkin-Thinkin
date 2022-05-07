package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/5 10:40
 */
public class JP713 {

    /**
     * 10, 5, 2, 6
     */
    public int numSubarrayProductLessThanK(int[] nums, int k) {
        int count = 0;
        int sum = 0;
        for (int i = 0; i<nums.length; i++) {
            sum = nums[i];
            if (sum < k)
                count++;
            for (int j = i + 1; j<nums.length; j++) {
                sum *= nums[j];
                if (sum < k)
                    count++;
                else
                    break;
            }
        }

        return count;
    }

    @Test
    public void test() {
        System.out.println(numSubarrayProductLessThanK(new int[] {1, 2, 3}, 0));
    }
}
