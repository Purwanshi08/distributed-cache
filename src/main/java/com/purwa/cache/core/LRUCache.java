package com.purwa.cache.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Least-Recently-Used cache, O(1) get/put via a HashMap + intrusive doubly
 * linked list (deliberately not using LinkedHashMap's accessOrder mode —
 * the point of this class is to demonstrate the underlying mechanics).
 *
 * Not thread-safe on its own; wrap with {@link com.purwa.cache.concurrency.ThreadSafeCache}
 * for concurrent use.
 */
public class LRUCache<K, V> implements Cache<K, V> {

    private class Node {
        K key;
        CacheEntry<V> entry;
        Node prev, next;

        Node(K key, CacheEntry<V> entry) {
            this.key = key;
            this.entry = entry;
        }
    }

    private final int capacity;
    private final Map<K, Node> map;
    private final Node head; // sentinel: head.next is most recently used
    private final Node tail; // sentinel: tail.prev is least recently used

    public LRUCache(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.head = new Node(null, null);
        this.tail = new Node(null, null);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public V get(K key) {
        Node node = map.get(key);
        if (node == null) return null;
        if (node.entry.isExpired()) {
            removeNode(node);
            map.remove(key);
            return null;
        }
        moveToFront(node);
        return node.entry.getValue();
    }

    @Override
    public void put(K key, V value) {
        put(key, value, -1);
    }

    @Override
    public void put(K key, V value, long ttlMillis) {
        CacheEntry<V> entry = new CacheEntry<>(value, ttlMillis);
        Node existing = map.get(key);
        if (existing != null) {
            existing.entry = entry;
            moveToFront(existing);
            return;
        }
        Node node = new Node(key, entry);
        map.put(key, node);
        addToFront(node);
        if (map.size() > capacity) {
            Node lru = tail.prev;
            removeNode(lru);
            map.remove(lru.key);
        }
    }

    @Override
    public void remove(K key) {
        Node node = map.remove(key);
        if (node != null) removeNode(node);
    }

    @Override
    public boolean containsKey(K key) {
        Node node = map.get(key);
        return node != null && !node.entry.isExpired();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
        head.next = tail;
        tail.prev = head;
    }

    private void addToFront(Node node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToFront(Node node) {
        removeNode(node);
        addToFront(node);
    }
}
