package xyz.scootaloo.thinking.pack;

import xyz.scootaloo.thinking.pack.ds.ListNode;

/**
 * sealed
 *
 * @author flutterdash@qq.com
 * @since 2022/4/8 18:32
 */
public class JP148v3 {

    private final int subSeqLimit = 2;
    private int subSeqSize = 0;

    public ListNode sortList(ListNode head) {
        if (head == null) return null;
        if (head.next == null) return head;

        return quicklySort(head);
    }

    private ListNode quicklySort(ListNode head) {
        ListNode right = split(head);
        ListNode left = head;
        int seqSize = subSeqSize;
        left = simpleSort(left, seqSize);
        right = simpleSort(right, seqSize);
        return marge(left, right);
    }

    private ListNode split(ListNode head) {
        ListNode fast = head.next, slow = head;
        subSeqSize = 1;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow.next;
    }

    private ListNode simpleSort(ListNode head, int size) {
        if (size <= subSeqLimit) {
            if (size == 1) {
                return head;
            } else {
                ListNode tail = head.next;
                if (head.val < tail.val) {
                    return head;
                } else {
                    tail.next = head;
                    head.next = null;
                    return tail;
                }
            }
        } else {
            return quicklySort(head);
        }
    }

    private ListNode marge(ListNode left, ListNode right) {
        ListNode head, pointer;
        if (left.val < right.val) {
            head = left;
            left = left.next;
        } else {
            head = right;
            right = right.next;
        }

        pointer = head;
        while (left != null && right != null) {
            if (left.val < right.val) {
                pointer.next = left;
                left = left.next;
            } else {
                pointer.next = right;
                right = right.next;
            }
            pointer = pointer.next;
        }

        return head;
    }
}
