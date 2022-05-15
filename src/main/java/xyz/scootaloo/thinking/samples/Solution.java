package xyz.scootaloo.thinking.samples;

import java.util.*;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/12 12:02
 */
public class Solution {

    public List<String> solve(String content) {
        TreeMap<String, String> map = collect(content);
        return searchAndMatch(map);
    }

    private TreeMap<String, String> collect(String raw) {
        raw = raw.replace('，', ',');
        String[] computable = raw.split(",");

        int pos = 0;
        String key, value;
        TreeMap<String, String> map = new TreeMap<>();
        while (pos < computable.length) {
            key = computable[pos++];
            if (pos < computable.length) {
                value = computable[pos++];
                map.put(key, value);
            }
        }

        return map;
    }

    private List<String> searchAndMatch(Map<String, String> map) {
        List<String> result = new LinkedList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (match(entry.getKey(), entry.getValue())) {
                String value = entry.getValue();
                if (value.contains("j") || value.contains("b")) {
                    result.add(entry.getValue());
                }
            }
        }
        return result;
    }

    private boolean match(String a, String b) {
        int aLen = a.length(), bLen = b.length();
        return ((double) aLen / (double) bLen) == ((double) (aLen / bLen));
    }

}
