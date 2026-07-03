package com.purwa.cache.sharding;

import com.purwa.cache.concurrency.ThreadSafeCache;
import com.purwa.cache.core.Cache;
import com.purwa.cache.factory.CacheFactory;
import com.purwa.cache.factory.EvictionPolicyType;

import java.util.HashMap;
import java.util.Map;

/**
 * Simulates a horizontally sharded cache: each shard is an independent,
 * thread-safe Cache instance, and keys are routed to shards via consistent
 * hashing. This is the piece that maps directly onto "design a distributed
 * cache" / "how would you scale this cart service" interview questions.
 */
public class DistributedCache<K, V> {

    private final ConsistentHashRing<CacheNode> ring;
    private final Map<CacheNode, Cache<K, V>> shards;
    private final EvictionPolicyType policyType;
    private final int perShardCapacity;

    public DistributedCache(int numShards, int perShardCapacity, EvictionPolicyType policyType, int virtualNodesPerShard) {
        this.ring = new ConsistentHashRing<>(virtualNodesPerShard);
        this.shards = new HashMap<>();
        this.policyType = policyType;
        this.perShardCapacity = perShardCapacity;

        for (int i = 0; i < numShards; i++) {
            CacheNode node = new CacheNode("shard-" + i);
            ring.addNode(node);
            shards.put(node, new ThreadSafeCache<>(CacheFactory.<K, V>create(policyType, perShardCapacity)));
        }
    }

    public void put(K key, V value) {
        put(key, value, -1);
    }

    public void put(K key, V value, long ttlMillis) {
        CacheNode node = ring.getNode(String.valueOf(key));
        if (node != null) shards.get(node).put(key, value, ttlMillis);
    }

    public V get(K key) {
        CacheNode node = ring.getNode(String.valueOf(key));
        if (node == null) return null;
        return shards.get(node).get(key);
    }

    public void remove(K key) {
        CacheNode node = ring.getNode(String.valueOf(key));
        if (node != null) shards.get(node).remove(key);
    }

    /** Adds a new shard at runtime — only a fraction of keys remap, thanks to consistent hashing. */
    public void addShard(String shardId) {
        CacheNode node = new CacheNode(shardId);
        ring.addNode(node);
        shards.put(node, new ThreadSafeCache<>(CacheFactory.<K, V>create(policyType, perShardCapacity)));
    }

    public int totalSize() {
        int total = 0;
        for (Cache<K, V> shard : shards.values()) {
            total += shard.size();
        }
        return total;
    }

    /** Exposes which shard a key would land on — useful for demos/tests. */
    public String shardFor(K key) {
        CacheNode node = ring.getNode(String.valueOf(key));
        return node == null ? null : node.getNodeId();
    }
}
