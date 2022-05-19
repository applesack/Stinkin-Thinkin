package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/17 12:16
 */
public class JP953 {

    /**
     * a     c
     * a   a b
     * a a b b c
     */
    public boolean isAlienSorted(String[] words, String order) {
        Map<Character, Integer> map = new HashMap<>();
        for(int i = 0; i < order.length(); i++) {
            map.put(order.charAt(i), i);
        }
        for(int i = 0; i < words.length - 1; i++) {
            for(int j = 0; j < words[i].length(); j++) {
                if(j == words[i + 1].length()) {
                    return false;
                }
                int a = map.get(words[i].charAt(j)), b = map.get(words[i + 1].charAt(j));
                if(a > b) {
                    return false;
                } else if(a < b) {
                    break;
                }
            }
        }
        return true;
    }

    @Test
    public void test() {
        isAlienSorted(new String[]{"hello","leetcode"}, "hlabcdefgijkmnopqrstuvwxyz");
    }

}
