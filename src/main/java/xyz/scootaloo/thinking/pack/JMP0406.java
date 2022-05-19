package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.thinking.pack.ds.TreeNode;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/16 20:57
 */
public class JMP0406 {

    public TreeNode inorderSuccessor(TreeNode root, TreeNode p) {
        if (p.right != null) {
            p = p.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        }

        TreeNode sentry = root, current = root;
        while (current != p) {
            if (current.val > p.val) {
                current = current.left;
            } else {
                current = current.right;
            }
            if (current.val > p.val) {
                sentry = current;
            }
        }

        if (sentry.val > p.val) {
            return sentry;
        } else {
            return null;
        }
    }

    @Test
    public void test() {
        TreeNode node1 = new TreeNode(1);
        TreeNode node2 = new TreeNode(2);
        TreeNode node3 = new TreeNode(3);
        TreeNode node4 = new TreeNode(4);
        TreeNode node5 = new TreeNode(5);
        TreeNode node6 = new TreeNode(6);

        node5.left = node3;
        node5.right = node6;

        node3.left = node2;
        node3.right = node4;

        node2.left = node1;

        System.out.println(inorderSuccessor(node5, node1));
        System.out.println(inorderSuccessor(node5, node2));
        System.out.println(inorderSuccessor(node5, node3));
        System.out.println(inorderSuccessor(node5, node4));
        System.out.println(inorderSuccessor(node5, node5));
        System.out.println(inorderSuccessor(node5, node6));
    }

}
