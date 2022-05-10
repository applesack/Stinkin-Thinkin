package xyz.scootaloo.thinking.pack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/8 22:26
 */
public class JP442 {

    /**
     * 0,1,2,3,4,5,6,7    index
     * 4,3,2,7,8,2,3,1
     * ^
     * 4,3,2,7,8,2,3,1
     * |     |
     * 7,3,2,4,8,2,3,1
     * |           |
     * 3,3,2,4,8,2,7,1
     * |   |
     * 2,3,3,4,8,2,7,1
     * | |
     * 3,2,3,4,8,2,7,1
     * |   | ========> 发现重复, 标记, 指针移动
     * 0,2,0,4,8,2,7,1
     *   ^                skip
     * 0,2,0,4,8,2,7,1
     *     ^              skip
     * 0,2,0,4,8,2,7,1
     *       ^            skip
     * 0,2,0,4,8,2,7,1
     *         ^
     * 0,2,0,4,1,2,7,8
     *         |     |
     * 0,2,0,4,1,2,7,8
     * |       | ====> 值缺失, 指针移动
     * 0,2,0,4,1,2,7,8
     *           ^
     * 0,2,0,4,1,2,7,8
     *   |       | ==> 发现重复, 标记, 指针移动
     * 0,0,0,4,1,0,7,8
     *             ^      skip
     * 0,0,0,4,1,0,7,8
     *               ^
     * 0,0,0,4,1,0,7,8
     *                 ^ finish
     */
    public List<Integer> findDuplicates(int[] nums) {
        List<Integer> res = new ArrayList<>();
        int next;
        int nextIdx;
        for (int i = 0; i<nums.length; i++) {
            if (nums[i] == 0)
                continue;
            while (nums[i] != 0 && nums[i] != (i + 1)) {
                nextIdx = nums[i] - 1;
                next = nums[nextIdx];
                if (nums[i] == next) {
                    res.add(next);
                    nums[i] = nums[nextIdx] = 0;
                    break;
                }
                nums[nextIdx] = nums[i];
                nums[i] = next;
            }
        }
        return res;
    }

}