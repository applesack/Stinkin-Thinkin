package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.thinking.pack.ds.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/1 10:59
 */
public class JP1305 {

    public List<Integer> getAllElements(TreeNode root1, TreeNode root2) {
        List<Integer> s1 = collect(root1);
        List<Integer> s2 = collect(root2);

        ArrayList<Integer> res = new ArrayList<>(s1.size() + s2.size());
        int pos1 = 0, pos2 = 0;
        while (pos1 < s1.size() && pos2 < s2.size()) {
            int s1Val = s1.get(pos1);
            int s2Val = s2.get(pos2);
            if (s1Val < s2Val) {
                res.add(s1Val);
                pos1++;
            } else {
                res.add(s2Val);
                pos2++;
            }
        }

        while (pos1 < s1.size()) {
            res.add(s1.get(pos1++));
        }

        while (pos2 < s2.size()) {
            res.add(s2.get(pos2++));
        }

        return res;
    }

    private List<Integer> collect(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();

        while (!stack.isEmpty() || root != null) {
            while (root != null) {
                stack.push(root);
                root = root.left;
            }

            root = stack.pop();
            res.add(root.val);
            root = root.right;
        }

        return res;
    }

    @Test
    public void test() {
        TreeNode root1 = new TreeNode(2);
        root1.left = new TreeNode(1);
        root1.right = new TreeNode(4);

        TreeNode root2 = new TreeNode(1);
        root2.left = new TreeNode(0);
        root2.right = new TreeNode(3);

        System.out.println(getAllElements(root1, root2));
    }

}
