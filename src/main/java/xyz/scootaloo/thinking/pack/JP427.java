package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/29 12:58
 */
public class JP427 {

    public Node construct(int[][] grid) {
        int side = grid.length;
        return construct(grid, side, 0, 0);
    }

    private Node construct(int[][] grid, int n, int baseR, int baseC) {
        if (n == 1)
            return leaf(grid[baseR][baseC] == 1);

        // n >= 2 && n == 2^x && 0 <= x <= 6
        int topLeftBox = 0, topRightBox = 0;
        int bottomLeftBox = 0, bottomRightBox = 0;

        int limitR = baseR + n;
        int limitC = baseC + n;
        int half = n / 2;

        for (int r = baseR; r < limitR; r++) {
            for (int c = baseC; c < limitC; c++) {
                int cur = grid[r][c];
                if (r - baseR < half) { // top
                    if (c - baseC < half) { // left
                        topLeftBox += cur;
                    } else { // right
                        topRightBox += cur;
                    }
                } else { // bottom
                    if (c - baseC < half) {
                        bottomLeftBox += cur;
                    } else {
                        bottomRightBox += cur;
                    }
                }
            }
        }

        int boxSum = topLeftBox + topRightBox + bottomLeftBox + bottomRightBox;
        if (boxSum == 0 || boxSum == (n * n)) {
            return leaf(!(boxSum == 0));
        }

        Node root = new Node(true, false);

        int area = half * half;
        if (topLeftBox == 0 || topLeftBox == area) {
            root.topLeft = leaf(topLeftBox == area);
        } else {
            root.topLeft = construct(grid, half, baseR, baseC);
        }

        if (topRightBox == 0 || topRightBox == area) {
            root.topRight = leaf(topRightBox == area);
        } else {
            root.topRight = construct(grid, half, baseR, baseC + half);
        }

        if (bottomLeftBox == 0 || bottomLeftBox == area) {
            root.bottomLeft = leaf(bottomLeftBox == area);
        } else {
            root.bottomLeft = construct(grid, half, baseR + half, baseC);
        }

        if (bottomRightBox == 0 || bottomRightBox == area) {
            root.bottomRight = leaf(bottomRightBox == area);
        } else {
            root.bottomRight = construct(grid, half, baseR + half, baseC + half);
        }

        return root;
    }

    private Node leaf(boolean val) {
        return new Node(val, true);
    }

    @Test
    public void test() {
        int[][] box = {
                {1, 1, 1, 1, 0, 0, 0, 0},
                {1, 1, 1, 1, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1, 1, 1, 1},
                {1, 1, 1, 1, 0, 0, 0, 0},
                {1, 1, 1, 1, 0, 0, 0, 0},
                {1, 1, 1, 1, 0, 0, 0, 0},
                {1, 1, 1, 1, 0, 0, 0, 0}
        };
        Node node = construct(box);
        System.out.println(node);
    }

    static class Node {
        public boolean val;
        public boolean isLeaf;
        public Node topLeft;
        public Node topRight;
        public Node bottomLeft;
        public Node bottomRight;


        public Node() {
            this.val = false;
            this.isLeaf = false;
            this.topLeft = null;
            this.topRight = null;
            this.bottomLeft = null;
            this.bottomRight = null;
        }

        public Node(boolean val, boolean isLeaf) {
            this.val = val;
            this.isLeaf = isLeaf;
            this.topLeft = null;
            this.topRight = null;
            this.bottomLeft = null;
            this.bottomRight = null;
        }

        private int getNum(boolean b) {
            if (b) return 1;
            return 0;
        }

        @Override
        public String toString() {
            return "Node(" +
                    "isLeaf=" + getNum(isLeaf) +
                    ", val=" + getNum(val) +
                    ')';
        }
    }
}
