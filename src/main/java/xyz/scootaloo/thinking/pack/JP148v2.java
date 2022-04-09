package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.thinking.pack.ds.ListNode;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/8 18:22
 */
public class JP148v2 {

    public ListNode sortList(ListNode head) {
        if (head == null) return null;
        if (head.next == null) return head;

        PriorityQueue<ListNode> queue = new PriorityQueue<>(Comparator.comparingInt(a -> a.val));
        ListNode pointer = head;
        while (pointer != null) {
            queue.add(pointer);
            pointer = pointer.next;
        }

        ListNode vHead = new ListNode(-1);
        pointer = vHead;
        while (!queue.isEmpty()) {
            pointer.next = queue.poll();
            pointer = pointer.next;
        }

        pointer.next = null;
        return vHead.next;
    }

    @Test
    public void test() {
        var res = sortList(JP148.input());
        System.out.println(res);
    }
}
