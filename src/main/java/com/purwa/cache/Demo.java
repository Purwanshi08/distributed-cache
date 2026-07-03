package com.purwa.cache;

import com.purwa.cache.core.Cache;
import com.purwa.cache.factory.CacheFactory;
import com.purwa.cache.factory.EvictionPolicyType;
import com.purwa.cache.sharding.DistributedCache;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("== LRU Cache Demo ==");
        Cache<String, String> lru = CacheFactory.create(EvictionPolicyType.LRU, 2);
        lru.put("a", "1");
        lru.put("b", "2");
        lru.get("a");              // a is now most recently used
        lru.put("c", "3");         // should evict b
        System.out.println("a=" + lru.get("a") + " b=" + lru.get("b") + " c=" + lru.get("c"));

        System.out.println("\n== LFU Cache Demo ==");
        Cache<String, String> lfu = CacheFactory.create(EvictionPolicyType.LFU, 2);
        lfu.put("x", "1");
        lfu.put("y", "2");
        lfu.get("x");
        lfu.get("x");               // x now has freq 3, y has freq 1
        lfu.put("z", "3");          // should evict y (lowest frequency)
        System.out.println("x=" + lfu.get("x") + " y=" + lfu.get("y") + " z=" + lfu.get("z"));

        System.out.println("\n== TTL Expiry Demo ==");
        Cache<String, String> ttlCache = CacheFactory.create(EvictionPolicyType.LRU, 5);
        ttlCache.put("temp", "value", 200);
        System.out.println("immediately: " + ttlCache.get("temp"));
        Thread.sleep(300);
        System.out.println("after 300ms: " + ttlCache.get("temp"));

        System.out.println("\n== Distributed Cache Demo (consistent hashing across 3 shards) ==");
        DistributedCache<String, String> dc = new DistributedCache<>(3, 10, EvictionPolicyType.LRU, 50);
        for (int i = 0; i < 10; i++) {
            String key = "key" + i;
            dc.put(key, "val" + i);
            System.out.println(key + " -> " + dc.shardFor(key));
        }
        System.out.println("get key5 = " + dc.get("key5"));
        System.out.println("total size across shards = " + dc.totalSize());
    }
}
