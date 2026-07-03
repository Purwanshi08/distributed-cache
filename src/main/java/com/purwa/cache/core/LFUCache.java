package com.purwa.cache.core;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Least-Frequently-Used cache, O(1) get/put using the classic
 * "frequency bucket" technique: each frequency count maps to an
 * insertion-ordered set of keys at that frequency, so ties break FIFO
 * within the same frequency (standard LFU tie-break behaviour).
 *
 * Thread-safe via method-level synchronization (kept simple and correct
 * over fine-grained locking, since this class is usually wrapped by a
 * single shard lock in the distributed layer anyway).
 */
public class LFUCache<K, V> implements Cache<K, V> {

    private final int capacity;
    private final Map<K, CacheEntry<V>> valueMap;
    private final Map<K, Integer> freqMap;
    private final Map<Integer, LinkedHashSet<K>> freqBuckets;
    private int minFreq;

    public LFUCache(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
        this.valueMap = new HashMap<>();
        this.freqMap = new HashMap<>();
        this.freqBuckets = new HashMap<>();
        this.minFreq = 0;
    }

    @Override
    public synchronized V get(K key) {
        CacheEntry<V> entry = valueMap.get(key);
        if (entry == null) return null;
        if (entry.isExpired()) {
            evictKey(key);
            return null;
        }
        bumpFrequency(key);
        return entry.getValue();
    }

    @Override
    public void put(K key, V value) {
        put(key, value, -1);
    }

    @Override
    public synchronized void put(K key, V value, long ttlMillis) {
        if (capacity <= 0) return;
        CacheEntry<V> entry = new CacheEntry<>(value, ttlMillis);

        if (valueMap.containsKey(key)) {
            valueMap.put(key, entry);
            bumpFrequency(key);
            return;
        }
        if (valueMap.size() >= capacity) {
            evictLFU();
        }
        valueMap.put(key, entry);
        freqMap.put(key, 1);
        freqBuckets.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        minFreq = 1;
    }

    @Override
    public synchronized void remove(K key) {
        evictKey(key);
    }

    @Override
    public synchronized boolean containsKey(K key) {
        CacheEntry<V> entry = valueMap.get(key);
        return entry != null && !entry.isExpired();
    }

    @Override
    public synchronized int size() {
        return valueMap.size();
    }

    @Override
    public synchronized void clear() {
        valueMap.clear();
        freqMap.clear();
        freqBuckets.clear();
        minFreq = 0;
    }

    private void bumpFrequency(K key) {
        int freq = freqMap.get(key);
        LinkedHashSet<K> bucket = freqBuckets.get(freq);
        bucket.remove(key);
        if (bucket.isEmpty()) {
            freqBuckets.remove(freq);
            if (minFreq == freq) minFreq++;
        }
        freqMap.put(key, freq + 1);
        freqBuckets.computeIfAbsent(freq + 1, k -> new LinkedHashSet<>()).add(key);
    }

    private void evictLFU() {
        LinkedHashSet<K> bucket = freqBuckets.get(minFreq);
        if (bucket == null || bucket.isEmpty()) return;
        K keyToEvict = bucket.iterator().next(); // oldest key at the lowest frequency
        bucket.remove(keyToEvict);
        if (bucket.isEmpty()) freqBuckets.remove(minFreq);
        valueMap.remove(keyToEvict);
        freqMap.remove(keyToEvict);
    }

    private void evictKey(K key) {
        if (!valueMap.containsKey(key)) return;
        int freq = freqMap.get(key);
        LinkedHashSet<K> bucket = freqBuckets.get(freq);
        bucket.remove(key);
        if (bucket.isEmpty()) freqBuckets.remove(freq);
        valueMap.remove(key);
        freqMap.remove(key);
    }
}
