package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/25 17:56
 */
public class JP398 {

    static class Solution {
        private final HashMap<Integer, Integer> single = new HashMap<>();
        private final HashMap<Integer, IndexStore> multi = new HashMap<>();

        public Solution(int[] nums) {
            init(nums);
        }

        public int pick(int target) {
            if (single.containsKey(target)) {
                return single.get(target);
            } else {
                return multi.get(target).next();
            }
        }

        private void init(int[] nums) {
            for (int idx = 0; idx < nums.length; idx++) {
                int num = nums[idx];
                if (single.containsKey(num)) {
                    int ret = single.get(num);
                    store2multi(num, ret);
                    store2multi(num, idx);
                    single.remove(num);
                } else {
                    if (multi.containsKey(num)) {
                        store2multi(num, idx);
                    } else {
                        single.put(num, idx);
                    }
                }
            }
        }

        private void store2multi(int num, int idx) {
            IndexStore store = multi.get(num);
            if (store == null) {
                store = new IndexStore();
                store.indexes.add(idx);
                multi.put(num, store);
            } else {
                store.indexes.add(idx);
            }
        }

        static class IndexStore {
            int count = 0;
            final ArrayList<Integer> indexes = new ArrayList<>(2);

            int next() {
                int ret = indexes.get(count);
                count++;
                if (count >= indexes.size()) {
                    count = 0;
                }
                return ret;
            }
        }
    }

    @Test
    public void test() {
        int[] input = {1, 2, 3, 3, 3};
        Solution s = new Solution(input);
        System.out.println(s.pick(3));
        System.out.println(s.pick(1));
        System.out.println(s.pick(3));
        System.out.println(s.pick(3));
        System.out.println(s.pick(3));
        System.out.println(s.pick(3));
    }

}
