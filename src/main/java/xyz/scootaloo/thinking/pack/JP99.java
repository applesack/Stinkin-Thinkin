package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.thinking.pack.ds.TreeNode;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/5 10:56
 */
public class JP99 {

    private TreeNode t1, t2, pre;

    public void recoverTree(TreeNode root) {
        inorder(root);
        int tmp = t1.val;
        t1.val = t2.val;
        t2.val = tmp;
    }

    private void inorder(TreeNode root) {
        if (root == null)
            return;

        inorder(root.left);

        if (pre != null && pre.val > root.val) {
            if (t1 == null)
                t1 = pre;
            t2 = root;
        }

        pre = root;
        inorder(root.left);
    }

    @Test
    public void test() {
        TreeNode node1 = new TreeNode(1);
        TreeNode node3 = new TreeNode(3);
        TreeNode node2 = new TreeNode(2);

        node1.left = node3;
        node3.right = node2;

        System.out.println();
        recoverTree(node1);
        System.out.println();
    }

}
