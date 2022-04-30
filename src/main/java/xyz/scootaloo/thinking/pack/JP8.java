package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/26 11:52
 */
public class JP8 {

    public int myAtoi(String s) {
        if (s.length() == 0) {
            return 0;
        }

        int index = 0;
        while (index < s.length() && s.charAt(index) == ' ') {
            index++;
        }

        if (index == s.length()) {
            return 0;
        }

        int sign = 1;
        long store = 0;

        long upBorder = Integer.MAX_VALUE;
        long downBorder = upBorder + 1;

        if (s.charAt(index) == '+') {
            index++;
        } else if (s.charAt(index) == '-') {
            sign = -1;
            index++;
        }

        if (index == s.length()) {
            return 0;
        }

        for (; index < s.length(); index++) {
            char ch = s.charAt(index);
            if (Character.isDigit(ch)) {
                if (store == 0) {
                    store = ch - '0';
                } else {
                    store *= 10;
                    store += ch - '0';
                }

                if (sign > 0 && store > upBorder) {
                    return truncate(store, sign);
                }
                if (sign < 0 && store > downBorder) {
                    return truncate(store, sign);
                }
            } else {
                return truncate(store, sign);
            }
        }

        return truncate(store, sign);
    }

    private int truncate(long num, int sign) {
        if (sign > 0) {
            if (num > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else {
                return (int) num;
            }
        } else {
            num = -num;
            if (num < Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            } else {
                return (int) num;
            }
        }
    }

    @Test
    public void test() {
        System.out.println(myAtoi("42"));
        System.out.println(myAtoi("      -42"));
        System.out.println(myAtoi("4193 hello world"));
        System.out.println(myAtoi("4193567676887989"));
        System.out.println(myAtoi("-4193567676887989"));
        System.out.println(myAtoi("214748364888888888888888888888"));
        System.out.println(myAtoi("  "));
        System.out.println(myAtoi("  - "));
        System.out.println(myAtoi("  -"));
    }

}
