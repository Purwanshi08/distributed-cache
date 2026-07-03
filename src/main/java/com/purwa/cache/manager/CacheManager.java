package com.purwa.cache.manager;

import com.purwa.cache.factory.EvictionPolicyType;
import com.purwa.cache.sharding.DistributedCache;

/**
 * Singleton pattern (double-checked locking, volatile field) — a single
 * shared entry point for the app's cache, the way a real service would
 * expose one CacheManager rather than letting every class construct its
 * own DistributedCache.
 */
public class CacheManager {

    private static volatile CacheManager instance;
    private final DistributedCache<String, Object> distributedCache;

    private CacheManager() {
        this.distributedCache = new DistributedCache<>(4, 1000, EvictionPolicyType.LRU, 100);
    }

    public static CacheManager getInstance() {
        if (instance == null) {
            synchronized (CacheManager.class) {
                if (instance == null) {
                    instance = new CacheManager();
                }
            }
        }
        return instance;
    }

    public DistributedCache<String, Object> getCache() {
        return distributedCache;
    }
}
