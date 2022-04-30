package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/28 11:56
 */
public class JP905 {

    public int[] sortArrayByParity(int[] nums) {
        int l = 0, r = nums.length - 1, tmp;
        while (l < r) {
            while (l < r && nums[l] % 2 == 0) {
                l++;
            }

            while (l<r && nums[r] % 2 == 1) {
                r--;
            }

            if (nums[l] % 2 == 1 && nums[r] % 2 == 0) {
                tmp = nums[l];
                nums[l] = nums[r];
                nums[r] = tmp;

                l++;
                r--;
            }
        }
        return nums;
    }

    @Test
    public void test() {
        int[] input1 = {0};
        System.out.println(Arrays.toString(sortArrayByParity(input1)));
    }
}
