package xyz.scootaloo.thinking.pack.samples;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int len = scanner.nextInt();
        int count = scanner.nextInt();
        String chStr = scanner.next();
        String str = scanner.next();
        System.out.print(solve(chStr.charAt(0), count, str));
    }

    private static int solve(char c, int count, String line) {
        int limit = line.length();
        int result = 0, tmp;
        char[] chars = line.toCharArray();
        for (int skip = 1; skip < limit; skip++) {
            int bound = limit - skip;
            for (int l = 0; l <= bound; l++) {
                int r = l + skip - 1;
                tmp = 0;
                for (int i = l; i <= r; i++) {
                    if (chars[i] == c) {
                        tmp++;
                    }
                }
                if (tmp == count) {
                    result++;
                }
            }
        }
        return result;
    }

}
