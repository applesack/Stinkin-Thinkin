package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.thinking.pack.ds.ListNode;

import java.util.TreeMap;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/8 17:33
 */
public class JP148 {

    static class NodeWrapper {
        public static int GLOBAL_VERSION = 0;
        public final int version;
        public final ListNode node;

        NodeWrapper(ListNode node) {
            this.node = node;
            this.version = GLOBAL_VERSION;
            GLOBAL_VERSION++;
        }
    }

    public ListNode sortList(ListNode head) {
        if (head == null) return null;
        if (head.next == null) return head;

        Object singleton = new Object();
        TreeMap<NodeWrapper, Object> tree = new TreeMap<>((a, b) -> {
            if (a.node.val != b.node.val) {
                return a.node.val - b.node.val;
            } else {
                return a.version - b.version;
            }
        });

        ListNode pointer = head;
        while (pointer != null) {
            tree.put(new NodeWrapper(pointer), singleton);
            pointer = pointer.next;
        }

        ListNode vHead = new ListNode(-1);
        pointer = vHead;
        for (NodeWrapper node : tree.keySet()) {
            pointer.next = node.node;
            pointer = pointer.next;
        }

        pointer.next = null;
        return vHead.next;
    }

    @Test
    public void test() {
        var res = sortList(input());
        System.out.println(res);
    }

    public static ListNode input() {
        var node1 = new ListNode(4);
        var node2 = new ListNode(2);
        var node3 = new ListNode(1);
        var node4 = new ListNode(3);
        var node5 = new ListNode(2);
        node1.next = node2;
        node2.next = node3;
        node3.next = node4;
        node4.next = node5;
        return node1;
    }
}
