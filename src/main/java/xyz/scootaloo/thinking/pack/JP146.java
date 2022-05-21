package xyz.scootaloo.thinking.pack;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flutterdash@qq.com
 * @since 2022/5/20 19:50
 */
public class JP146 {

    @Test
    public void test() {
        LRUCache cache = new LRUCache(2);
        cache.put(2, 1);
        cache.put(1, 1);
        cache.put(2, 3);
        cache.put(4, 1);
        System.out.println(cache.get(1));
        System.out.println(cache.get(2));
    }

    static class LRUCache {
        private final int maxSize;
        private final Map<Integer, Node> map = new HashMap<>();

        private final Node head = new Node(1, 1);
        private final Node tail = new Node(0, 0);

        public LRUCache(int capacity) {
            maxSize = capacity;

            head.next = tail;
            tail.prev = head;
        }

        public int get(int key) {
            Node node = map.get(key);
            if (node != null) {
                moveToHead(node);
                return node.value;
            }
            return -1;
        }

        public void put(int key, int value) {
            Node node = map.get(key);
            if (node != null) {
                node.value = value;
                moveToHead(node);
                return;
            }

            doPut(key, value);
            if (map.size() > maxSize) {
                Node theTail = removeTail();
                map.remove(theTail.key);
            }
        }

        private void doPut(int key, int value) {
            Node node = new Node(key, value);
            map.put(key, node);
            addToHead(node);
        }

        private void moveToHead(Node node) {
            addToHead(delete(node));
        }

        private void addToHead(Node node) {
            node.prev = head;
            node.next = head.next;

            head.next.prev = node;
            head.next = node;
        }

        private Node removeTail() {
            Node rsl = tail.prev;
            return delete(rsl);
        }

        private Node delete(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            return node;
        }

        private static class Node {
            final int key;
            int value;
            Node prev;
            Node next;

            public Node(int key, int value) {
                this.key = key;
                this.value = value;
            }
        }
    }

}
