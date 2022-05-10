package xyz.scootaloo.thinking.samples;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/10 13:27
 */
public class PrefixMatching {

    public List<String> match(String pattern, List<String> input) {
        List<Pair> result = new ArrayList<>(16);
        for (String item : input) {
            int commonPrefix = compare(pattern, item);
            if (commonPrefix > 0) {
                result.add(new Pair(commonPrefix, item));
            }
        }
        return sortResult(result);
    }

    private List<String> sortResult(List<Pair> result) {
        result.sort(Comparator.comparingInt(a -> a.prefix));
        return result.stream().map(r -> r.content).collect(Collectors.toList());
    }

    private int compare(String source, String other) {
        if (source.isBlank() || other.isBlank()) {
            return 0;
        }

        int sourceValidPos = 0, otherValidPos = 0;
        int matched = 0;
        char s, o;
        while (sourceValidPos < source.length() && otherValidPos < other.length()) {
            s = source.charAt(sourceValidPos);
            if (isIgnoreItem(s)) {
                sourceValidPos++;
                continue;
            }
            o = other.charAt(otherValidPos);
            if (isIgnoreItem(o)) {
                otherValidPos++;
                continue;
            }
            if (match(s, o)) {
                matched++;
                sourceValidPos++;
                otherValidPos++;
            } else {
                break;
            }
        }

        return matched;
    }

    private boolean match(char c1, char c2) {
        return Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }

    private boolean isIgnoreItem(char c) {
        return c == ' ' || c == ',' || c == '[' || c == ']' || c == '\'' || c == '{'
                || c == '}' || c == '*';
    }

    private static class Pair {
        final int prefix;
        final String content;

        public Pair(int prefix, String content) {
            this.prefix = prefix;
            this.content = content;
        }
    }

    /**
     * ------------------------------------------------
     * <p>
     * T E S T
     * <p>
     * ------------------------------------------------
     */
    @Test
    public void test() {
        List<String> collect = new LinkedList<>();

        collect.add("[Big Dreams Small Spaces, TV Show, United Kingdom, British TV Shows " +
                " International TV Shows  Reality TV, Monty Don, " +
                ", Writer and presenter Monty Don helps England's " +
                "budding horticulturists plant and grow the gardens of their dreams., 2017]");
        collect.add("[Igor, Movie, United States  France, Children " +
                "& Family Movies, John Cusack  Steve Buscemi  Sean Hayes  Molly Shannon " +
                " Eddie Izzard  Jennifer Coolidge  Jay Leno, Tony Leondis," +
                " Igor the brilliant but deformed assistant to mad Dr." +
                " Glickenstein dreams of winning the Evil Science Fair and" +
                " the heart of village beauty Gretchen., 2008]");
        collect.add("[Yu-Gi-Oh! Arc-V, TV Show, Japan  Canada," +
                " Anime Series  Kids' TV, Mike Liscio  Emily Bauer" +
                "  Billy Bob Thompson  Alyson Leigh Rosenfeld" +
                "  Michael Crouch, , Now that he's discovered the" +
                " Pendulum Summoning technique Yuya's dream of" +
                " becoming the greatest \"dueltainer\" is " +
                "in reach – but it won't be easy!, 2015]");

        List<String> result = match(" ,[,[,b,I,g,D ream", collect);
        for (String rest : result) {
            System.out.println(rest);
        }
    }

}
