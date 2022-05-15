package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

import javax.sound.midi.Soundbank;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/13 13:22
 */
public class JMP0105 {

    /**
     * abababa
     * bababa
     */
    public boolean oneEditAway(String first, String second) {
        if (first.length() < second.length()) {
            String tmp = second;
            second = first;
            first = tmp;
        }

        int fSize = first.length(), sSize = second.length();
        if (Math.abs(fSize - sSize) > 1) {
            return false;
        }

        if (fSize == sSize) {
            boolean valid = true;
            for (int i = 0; i < fSize; i++) {
                if (first.charAt(i) != second.charAt(i)) {
                    if (valid) {
                        valid = false;
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }

        int fLeft = 0, fRight = fSize - 1;
        int sLeft = 0, sRight = sSize - 1;
        while (fLeft < fSize && sLeft < sSize) {
            if (first.charAt(fLeft) == second.charAt(sLeft)) {
                fLeft++;
                sLeft++;
            } else {
                fLeft--;
                sLeft--;
                break;
            }
        }
        if (sLeft > sSize) {
            return true;
        }
        while (fRight >= 0 && sRight >= 0) {
            if (first.charAt(fRight) == second.charAt(sRight)) {
                fRight--;
                sRight--;
            } else  {
                fRight++;
                sRight++;
                break;
            }
        }
        if (sRight < 0) {
            return true;
        }

        return Math.abs(fRight - fLeft) <= 2;
    }

    @Test
    public void test() {
        System.out.println(oneEditAway("pale", "pal"));
        System.out.println(oneEditAway("pales", "pal"));
        System.out.println(oneEditAway(
                "intention",
                "execution"
        ));
        System.out.println(oneEditAway(
                "teacher",
                "bleacher"
        ));
        System.out.println(oneEditAway(
//               01234
                "teacher",
                "treacher"
//               | |
        ));
    }

}
