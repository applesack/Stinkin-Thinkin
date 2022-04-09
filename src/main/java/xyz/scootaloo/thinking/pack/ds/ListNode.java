package xyz.scootaloo.thinking.pack.ds;

/**
 * @author flutterdash@qq.com
 * @since 2022/4/8 17:34
 */
public class ListNode {

    public int val;
    public ListNode next;

    ListNode() {
    }

    public ListNode(int val) {
        this.val = val;
    }

    ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }
}
