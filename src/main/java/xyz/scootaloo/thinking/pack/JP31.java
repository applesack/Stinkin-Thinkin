package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/25 18:24
 */
public class JP31 {

    /**
     * 1 2 3 4 5 9 8 7 6
     * 1 2 3 4 5 9 8 6 7
     * <p>
     * <p>
     * <p>
     * 3 4 2 1
     * 4 1 2 3
     * <p>
     * 1 2 3
     * 1 3 2
     * 2 1 3
     * 2 3 1 <
     * 3 1 2
     * 3 2 1
     *
     * @param nums ignore
     */
    public void nextPermutation(int[] nums) {
        if (nums.length < 2)
            return;
        int idx = nums.length - 1;
        int max = nums[idx];
        int maxIdx = idx;
        for (; idx > 0; idx--) {
            if (nums[idx] > nums[idx - 1]) {
                max = nums[idx];
                maxIdx = idx;
                break;
            }
        }

        for (; idx >= 0; idx--) {
            if (nums[idx] < max) {
                break;
            }
        }

        if (idx >= 0) {
            int min = nums[idx];
            nums[idx] = max;
            nums[maxIdx] = min;
            partSort(nums, idx + 1);
        } else {
            int mid = nums.length / 2;
            int tmp;
            for (idx = 0; idx < mid; idx++) {
                tmp = nums[idx];
                nums[idx] = nums[nums.length - idx - 1];
                nums[nums.length - idx - 1] = tmp;
            }
        }
    }

    private void partSort(int[] nums, int offset) {
        if (offset == nums.length - 1)
            return;
        Arrays.sort(nums, offset, nums.length);
    }

    @Test
    public void test() {
//        int[] input = {3, 4, 2, 1};
        int[] input = {1, 3, 2};
        nextPermutation(input);
        System.out.println(Arrays.toString(input));
    }
}
