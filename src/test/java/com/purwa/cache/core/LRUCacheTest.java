package com.purwa.cache.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LRUCacheTest {

    @Test
    void testBasicPutGet() {
        Cache<String, Integer> cache = new LRUCache<>(2);
        cache.put("a", 1);
        assertEquals(1, cache.get("a"));
    }

    @Test
    void testEvictionOrder() {
        Cache<String, Integer> cache = new LRUCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");     // a becomes most recently used
        cache.put("c", 3);  // should evict b, not a
        assertNull(cache.get("b"));
        assertEquals(1, cache.get("a"));
        assertEquals(3, cache.get("c"));
    }

    @Test
    void testUpdateExistingKeyRefreshesRecency() {
        Cache<String, Integer> cache = new LRUCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("a", 10); // refresh a's recency
        cache.put("c", 3);  // should evict b
        assertNull(cache.get("b"));
        assertEquals(10, cache.get("a"));
    }

    @Test
    void testTTLExpiry() throws InterruptedException {
        Cache<String, Integer> cache = new LRUCache<>(2);
        cache.put("a", 1, 100);
        assertEquals(1, cache.get("a"));
        Thread.sleep(150);
        assertNull(cache.get("a"));
    }

    @Test
    void testRemoveAndClear() {
        Cache<String, Integer> cache = new LRUCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.remove("a");
        assertNull(cache.get("a"));
        assertEquals(1, cache.size());
        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    void testInvalidCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<String, Integer>(0));
    }
}
