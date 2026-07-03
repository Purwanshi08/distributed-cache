package com.purwa.cache.core;

/**
 * Wraps a cached value with expiry metadata. Immutable — a new entry is
 * created on every put, which keeps the eviction data structures simple to
 * reason about (no in-place mutation races on the entry itself).
 */
public class CacheEntry<V> {

    private final V value;
    private final long expiryTimestamp; // -1 means "never expires"

    public CacheEntry(V value, long ttlMillis) {
        this.value = value;
        this.expiryTimestamp = ttlMillis > 0 ? System.currentTimeMillis() + ttlMillis : -1;
    }

    public V getValue() {
        return value;
    }

    public boolean isExpired() {
        return expiryTimestamp != -1 && System.currentTimeMillis() > expiryTimestamp;
    }
}
