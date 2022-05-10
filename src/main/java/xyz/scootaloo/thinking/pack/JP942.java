package xyz.scootaloo.thinking.pack;

import java.nio.charset.StandardCharsets;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/9 13:46
 */
public class JP942 {

    public int[] diStringMatch(String s) {
        char[] chars = s.toCharArray();
        int left = 0, right = chars.length, limit = right;
        int[] res = new int[limit + 1];
        for (int i = 0; i<limit; i++) {
            if (chars[i] == 'I') {
                res[i] = left++;
            } else {
                res[i] = right--;
            }
        }
        res[limit] = right;
        return res;
    }

}
