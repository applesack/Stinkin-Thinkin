package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/15 15:38
 */
public class JP812 {

    public double largestTriangleArea(int[][] points) {
        int limit = points.length;
        double maxArea = Double.MIN_VALUE, area;
        int[] p1, p2, p3;
        for (int k1 = 0; k1 < limit; k1++) {
            p1 = points[k1];
            for (int k2 = k1 + 1; k2 < limit; k2++) {
                p2 = points[k2];
                for (int k3 = k2 + 1; k3 < limit; k3++) {
                    p3 = points[k3];
                    area = 0.5 * Math.abs(p1[0] * p2[1] +
                            p2[0] * p3[1] +
                            p3[0] * p1[1] -
                            p1[0] * p3[1] -
                            p2[0] * p1[1] -
                            p3[0] * p2[1]
                    );
                    if (area > maxArea) {
                        maxArea = area;
                    }
                }
            }
        }
        return maxArea;
    }

    @Test
    public void test() {
        int[][] input = new int[][] {
                {0, 0},
                {0, 1},
                {1, 0},
                {0, 2},
                {2, 0},
        };
        System.out.println(largestTriangleArea(input));
    }

}
