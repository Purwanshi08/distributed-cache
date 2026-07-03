package com.purwa.cache.sharding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Consistent hashing ring with virtual nodes, used to route cache keys to
 * shards. Virtual nodes smooth out load distribution — with too few points
 * per physical node, one shard can end up owning a disproportionate share
 * of the key space just by chance.
 *
 * On node add/remove, only the keys owned by that node's ring segments
 * remap — not the whole keyspace. That's the core property that makes this
 * preferable to plain modulo hashing when shards scale up/down.
 */
public class ConsistentHashRing<T> {

    private final SortedMap<Long, T> ring = new TreeMap<>();
    private final int virtualNodesPerNode;

    public ConsistentHashRing(int virtualNodesPerNode) {
        this.virtualNodesPerNode = virtualNodesPerNode;
    }

    public void addNode(T node) {
        for (int i = 0; i < virtualNodesPerNode; i++) {
            ring.put(hash(node.toString() + "#VN" + i), node);
        }
    }

    public void removeNode(T node) {
        for (int i = 0; i < virtualNodesPerNode; i++) {
            ring.remove(hash(node.toString() + "#VN" + i));
        }
    }

    public T getNode(String key) {
        if (ring.isEmpty()) return null;
        long hash = hash(key);
        SortedMap<Long, T> tailMap = ring.tailMap(hash);
        long targetHash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        return ring.get(targetHash);
    }

    public int ringSize() {
        return ring.size();
    }

    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes());
            long hash = 0;
            for (int i = 0; i < 8; i++) {
                hash = (hash << 8) | (digest[i] & 0xFF);
            }
            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
