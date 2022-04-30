package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.thinking.pack.ds.ListNode;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/9 22:13
 */
public class JP25 {

    public ListNode reverseKGroup(ListNode head, int k) {
        if (head == null)
            return null;
        if (head.next == null)
            return head;
        if (k == 0 || k == 1)
            return head;

        ListNode[] bucket = new ListNode[k];
        ListNode vHead = new ListNode(0);
        ListNode pHead = vHead, pTail = vHead, pointer;
        int rest = getListLen(head);

        /* 1 -> 2 -> 3 -> 4 -> 5 -> 6 ...
         * 1 -> 2 ->[3 -> 4 -> 5]-> 6 ..
         * 1 -> 2 ->[5 -> 4 -> 3]-> 6 ..
         */

        pHead.next = head;
        while (k <= rest) {
            int count = 0;
            pointer = pTail.next;
            while (count < k) {
                bucket[count] = pointer;
                pointer = pointer.next;
                count++;
            }

            pTail = bucket[k - 1].next;

            reverse(bucket);
            rest -= k;

            pHead.next = bucket[0];
            bucket[k - 1].next = pTail;
            pTail = bucket[k - 1];
            pHead = pTail;
        }

        return vHead.next;
    }

    private void reverse(ListNode[] bucket) {
        ListNode tmp;
        int len = bucket.length;
        for (int i = 0; i < bucket.length / 2; i++) {
            tmp = bucket[i];
            bucket[i] = bucket[len - 1 - i];
            bucket[len - 1 - i] = tmp;
        }
        len--;
        for (int i = 0; i < len; i++) {
            bucket[i].next = bucket[i + 1];
        }
    }

    private int getListLen(ListNode head) {
        ListNode pointer = head;
        int len = 1;
        while (pointer.next != null) {
            pointer = pointer.next;
            len++;
        }
        return len;
    }

    @Test
    public void test() {
        ListNode head = reverseKGroup(input(), 2);
        System.out.println(head);
    }

    private ListNode input() {
        ListNode node1 = new ListNode(1);
        ListNode node2 = new ListNode(2);
        ListNode node3 = new ListNode(3);
        ListNode node4 = new ListNode(4);
        ListNode node5 = new ListNode(5);

        node1.next = node2;
        node2.next = node3;
        node3.next = node4;
        node4.next = node5;

        return node1;
    }
}
