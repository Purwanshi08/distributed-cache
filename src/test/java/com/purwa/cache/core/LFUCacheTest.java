package com.purwa.cache.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LFUCacheTest {

    @Test
    void testBasicPutGet() {
        Cache<String, Integer> cache = new LFUCache<>(2);
        cache.put("a", 1);
        assertEquals(1, cache.get("a"));
    }

    @Test
    void testEvictsLeastFrequentlyUsed() {
        Cache<String, Integer> cache = new LFUCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");
        cache.get("a"); // a now accessed 3x total (1 put + 2 gets), b accessed 1x
        cache.put("c", 3); // b has lowest frequency -> evicted
        assertNull(cache.get("b"));
        assertNotNull(cache.get("a"));
        assertNotNull(cache.get("c"));
    }

    @Test
    void testTiesBreakFIFO() {
        Cache<String, Integer> cache = new LFUCache<>(2);
        cache.put("a", 1); // freq 1
        cache.put("b", 2); // freq 1, inserted after a
        cache.put("c", 3); // both at freq 1 -> a evicted first (oldest)
        assertNull(cache.get("a"));
        assertNotNull(cache.get("b"));
        assertNotNull(cache.get("c"));
    }

    @Test
    void testTTLExpiry() throws InterruptedException {
        Cache<String, Integer> cache = new LFUCache<>(2);
        cache.put("a", 1, 100);
        assertEquals(1, cache.get("a"));
        Thread.sleep(150);
        assertNull(cache.get("a"));
    }

    @Test
    void testInvalidCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () -> new LFUCache<String, Integer>(-1));
    }
}
