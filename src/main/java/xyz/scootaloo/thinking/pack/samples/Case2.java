package xyz.scootaloo.thinking.pack.samples;

import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/12 14:30
 */
public class Case2 {

    public static class Matcher {
        private final Random random = new Random();
        private final Set<Integer> constant = new HashSet<>();
        private final List<Integer> rest = new ArrayList<>();

        public Matcher() {
            constant.add(42);
            constant.add(44);
        }

        public void refresh(int[] nums) {
            rest.clear();
            for (int num : nums) {
                if (!constant.contains(num)) {
                    rest.add(num);
                }
            }
        }

        public List<Integer> solve(int count) {
            Set<Integer> indies = new HashSet<>();
            List<Integer> result = prepareConstantList();
            while (result.size() < count) {
                int randIdx = random.nextInt(rest.size());
                if (!indies.contains(randIdx)) {
                    indies.add(randIdx);
                    result.add(rest.get(randIdx));
                }
            }

            Collections.shuffle(result);
            return result;
        }

        private List<Integer> prepareConstantList() {
            return new ArrayList<>(constant);
        }
    }

    @Test
    public void test() {
        Matcher matcher = new Matcher();
        matcher.refresh(new int[] { 11, 23, 45, 44, 17, 16 });
        System.out.println(matcher.solve(3));
        System.out.println(matcher.solve(3));
        System.out.println(matcher.solve(3));
        System.out.println(matcher.solve(3));
    }

}
