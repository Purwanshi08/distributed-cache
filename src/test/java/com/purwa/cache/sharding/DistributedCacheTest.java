package com.purwa.cache.sharding;

import com.purwa.cache.factory.EvictionPolicyType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistributedCacheTest {

    @Test
    void testPutAndGetAcrossShards() {
        DistributedCache<String, String> cache = new DistributedCache<>(3, 10, EvictionPolicyType.LRU, 50);
        for (int i = 0; i < 20; i++) {
            cache.put("key" + i, "value" + i);
        }
        for (int i = 0; i < 20; i++) {
            assertEquals("value" + i, cache.get("key" + i));
        }
    }

    @Test
    void testKeyConsistentlyRoutesToSameShard() {
        DistributedCache<String, String> cache = new DistributedCache<>(3, 10, EvictionPolicyType.LRU, 50);
        String shard1 = cache.shardFor("myKey");
        String shard2 = cache.shardFor("myKey");
        assertEquals(shard1, shard2);
    }

    @Test
    void testAddShardAtRuntime() {
        DistributedCache<String, String> cache = new DistributedCache<>(2, 30, EvictionPolicyType.LRU, 50);
        cache.addShard("shard-extra");
        for (int i = 0; i < 30; i++) {
            cache.put("key" + i, "v" + i);
        }
        assertEquals(30, cache.totalSize());
    }
}
