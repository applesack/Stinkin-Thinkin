package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/7 16:17
 */
public class JP433 {

    int solvation = Integer.MAX_VALUE;

    public int minMutation(String start, String end, String[] bank) {
        int[] genes = new int[bank.length];
        for (int i = 0; i < bank.length; i++) {
            genes[i] = extractTrait(bank[i], end);
        }
        int trait = extractTrait(end, end);
        boolean has = false;
        for (int gene : genes) {
            if ((gene ^ trait) == 0) {
                has = true;
                break;
            }
        }
        if (!has)
            return -1;

        solvation = Integer.MAX_VALUE;

        int startTrait = extractTrait(start, end);
        if (startTrait == trait)
            return 0;
        if (diff(startTrait, trait)) {
            return 1;
        }

        dfs(startTrait, genes, trait, new boolean[genes.length], 0);
        if (solvation == Integer.MAX_VALUE) {
            return -1;
        }
        return solvation;
    }

    private void dfs(int current, int[] genes, int end, boolean[] access, int count) {
        if (current == end) {
            if (count < solvation) {
                solvation = count;
            }
            return;
        }

        for (int i = 0; i < genes.length; i++) {
            if (access[i])
                continue;
            if (diff(genes[i], current)) {
                access[i] = true;
                dfs(genes[i], genes, end, access, count + 1);
                access[i] = false;
            }
        }
    }

    private boolean diff(int a, int b) {
        if (a == b)
            return false;
        int rest = a ^ b;
        return rest > 0 && (rest & (rest - 1)) == 0;
    }

    private int extractTrait(String gene, String end) {
        int base = 1;
        for (int i = 0; i < 8; i++) {
            base <<= 1;
            if (gene.charAt(i) != end.charAt(i)) {
                base += 1;
            }
        }
        return base;
    }

    @Test
    public void test() {
        System.out.println(minMutation("AACCGGTT", "AACCGGTA", new String[]{
                "AACCGGTA"
        }));

        System.out.println(minMutation("AACCGGTT", "AAACGGTA", new String[]{
                "AACCGGTA", "AACCGCTA", "AAACGGTA"
        }));

        System.out.println(minMutation("AAAAACCC", "AACCCCCC", new String[]{
                "AAAACCCC","AAACCCCC","AACCCCCC"
        }));
    }

    @Test
    public void testFunc() {
        System.out.println(extractTrait("AACCGGTT", "AAACGGTA"));
        System.out.println(extractTrait("AACCGGTA", "AAACGGTA"));
        System.out.println(diff(
                extractTrait("AACCGGTT", "AAACGGTA"),
                extractTrait("AACCGGTA", "AAACGGTA")
        ));
    }

}
