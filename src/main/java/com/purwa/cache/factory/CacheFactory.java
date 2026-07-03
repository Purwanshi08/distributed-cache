package com.purwa.cache.factory;

import com.purwa.cache.core.Cache;
import com.purwa.cache.core.LFUCache;
import com.purwa.cache.core.LRUCache;

/**
 * Factory pattern: decouples callers from concrete cache implementations.
 * Adding a new eviction policy means adding an enum value + a case here —
 * callers never change.
 */
public final class CacheFactory {

    private CacheFactory() {
    }

    public static <K, V> Cache<K, V> create(EvictionPolicyType type, int capacity) {
        switch (type) {
            case LRU:
                return new LRUCache<>(capacity);
            case LFU:
                return new LFUCache<>(capacity);
            default:
                throw new IllegalArgumentException("Unsupported eviction policy: " + type);
        }
    }
}
