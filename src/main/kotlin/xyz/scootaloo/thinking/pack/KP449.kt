package xyz.scootaloo.thinking.pack

import cn.hutool.core.lang.tree.Tree
import org.junit.jupiter.api.Test
import xyz.scootaloo.thinking.lang.TestDsl
import xyz.scootaloo.thinking.pack.ds.TreeNode
import java.util.LinkedList

/**
 * @author flutterdash@qq.com
 * @since 2022/5/11 21:20
 */
class KP449 : TestDsl {

    class Codec() {
        // Encodes a URL to a shortened URL.
        // 使用中序遍历，将内容转换为一个排序数组
        fun serialize(root: TreeNode?): String {
            root ?: return ""
            val buff = StringBuilder()
            inorder(root, buff)
            return buff.toString()
        }

        // Decodes your encoded data to tree.
        fun deserialize(data: String): TreeNode? {
            if (data.isEmpty()) return null
            val root = TreeNode(data[0].toInt())
            build(root, data, 1)
            return root
        }

        private fun build(root: TreeNode, data: String, pos: Int): Int {
            var offset = pos
            if (data[offset] == '焯') {
                offset++
            } else {
                root.left = TreeNode(data[offset].toInt())
                offset = build(root.left, data, offset + 1)
            }

            if (data[offset] == '焯') {
                offset++
            } else {
                root.right = TreeNode(data[offset].toInt())
                offset = build(root.right, data, offset + 1)
            }

            return offset
        }

        private fun inorder(root: TreeNode, res: StringBuilder) {
            res.append(root.`val`.toChar())

            if (root.left != null) {
                inorder(root.left, res)
            } else {
                res.append('焯')
            }

            if (root.right != null) {
                inorder(root.right, res)
            } else {
                res.append('焯')
            }
        }
    }

    @Test
    fun test() {
        val node1 = TreeNode(1)
        val node2 = TreeNode(2)
        val node3 = TreeNode(3)
        val node4 = TreeNode(4)
        val node5 = TreeNode(5)
        val node6 = TreeNode(6)
        val node7 = TreeNode(7)

        node4.left = node2
        node2.left = node1
        node2.right = node3

        node4.right = node6
        node6.left = node5
        node6.right = node7

        val codec = Codec()
        val content = codec.serialize(node4)
        content.log()
        val tree = codec.deserialize(content)
        tree.log()
    }

    @Test
    fun test2() {
        val node2 = TreeNode(2)
        val node1 = TreeNode(1)
        node1.right = node2
        val codec = Codec()
        val content = codec.serialize(node1)
        val tree = codec.deserialize(content)
        tree.log()
    }

    @Test
    fun test3() {
        '.'.code.log()
        '-'.code.log()
        '+'.code.log()
        '焯'.code.log()
    }

}