package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/9 22:03
 */
public class JP4 {

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int[] array = new int[nums1.length + nums2.length];

        // merge
        int pos = 0, posA = 0, posB = 0;
        while (posA < nums1.length && posB < nums2.length) {
            if (nums1[posA] < nums2[posB]) {
                array[pos++] = nums1[posA++];
            } else {
                array[pos++] = nums2[posB++];
            }
        }

        if (posA < nums1.length) {
            while (posA < nums1.length) {
                array[pos++] = nums1[posA++];
            }
        }
        if (posB < nums2.length) {
            while (posB < nums2.length) {
                array[pos++] = nums2[posB++];
            }
        }

        int mid;
        int arrayLen = array.length;
        if (arrayLen % 2 != 0) {
            // 0, 1, 2, 3, 4
            //       ^
            mid = arrayLen / 2;
            return array[mid];
        } else {
            if (arrayLen == 2) {
                return (array[0] + array[1]) / 2D;
            } else {
                // 0, 1, 2, 3
                //    ^  ^
                mid = arrayLen / 2;
                return (array[mid] + array[mid - 1]) / 2D;
            }
        }
    }

    @Test
    public void test() {
        var arr1 = new int[] {1, 2};
        var arr2 = new int[] {3, 4};
        System.out.println(findMedianSortedArrays(arr1, arr2));
    }
}
