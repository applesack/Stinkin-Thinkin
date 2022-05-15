package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/12 13:41
 */
public class JP944 {

    public int minDeletionSize(String[] strs) {
        int row = strs.length;
        int col = strs[0].length();

        char pre;
        int count = 0;
        for (int c = 0; c < col; c++) {
            pre = strs[0].charAt(c);
            for (int r = 1; r < row; r++) {
                if (strs[r].charAt(c) < pre) {
                    count++;
                    break;
                } else {
                    pre = strs[r].charAt(c);
                }
            }
        }

        return count;
    }

    @Test
    public void test() {
        String[] input = new String[]{
                "cbc",
                "daf",
                "ghi"
        };
        System.out.println(minDeletionSize(input));
    }

    @Test
    public void test1() {
        String[] input = new String[]{
                "cba",
                "daf",
                "ghi"
        };
        System.out.println(minDeletionSize(input));
    }

    @Test
    public void test2() {
        String[] input = new String[]{
                "a", "b"
        };
        System.out.println(minDeletionSize(input));
    }

    @Test
    public void test3() {
        String[] input = new String[]{
                "zyx", "wvu", "tsr"
        };
        System.out.println(minDeletionSize(input));
    }

}
