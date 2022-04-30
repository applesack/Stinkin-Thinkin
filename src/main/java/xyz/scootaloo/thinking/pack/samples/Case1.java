package xyz.scootaloo.thinking.pack.samples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/20 17:28
 */
public class Case1 {

    public static void main(String[] args) {

    }

    public List<Map.Entry<String, Integer>> resolve(String text, List<String> list) {
        List<Map.Entry<String, Integer>> result = new ArrayList<>();
        for (String item : list) {
            int prefix = commonPrefix(text, item);
            if (prefix > 0) {
                result.add(new Entry(item, prefix));
            }
        }
        return result;
    }

    private int commonPrefix(String a, String b) {
        int min = Math.min(a.length(), b.length());
        int matched = 0;
        for (int i = 0; i<min; i++) {
            if (matchIgnoreCase(a.charAt(i), b.charAt(i))) {
                matched++;
            } else {
                break;
            }
        }

        return matched;
    }

    private boolean matchIgnoreCase(char a, char b) {
        String aStr = String.valueOf(a), bStr = String.valueOf(b);
        return aStr.equalsIgnoreCase(bStr);
    }

    static class Entry implements Map.Entry<String, Integer> {
        public String key;
        public Integer value;

        public Entry(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer setValue(Integer value) {
            Integer oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key='" + key + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

}
