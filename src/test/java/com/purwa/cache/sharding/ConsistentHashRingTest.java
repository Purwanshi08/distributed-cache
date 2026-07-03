package com.purwa.cache.sharding;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConsistentHashRingTest {

    @Test
    void testKeyAlwaysMapsToSameNode() {
        ConsistentHashRing<CacheNode> ring = new ConsistentHashRing<>(50);
        ring.addNode(new CacheNode("node-1"));
        ring.addNode(new CacheNode("node-2"));
        ring.addNode(new CacheNode("node-3"));

        CacheNode first = ring.getNode("some-key");
        CacheNode second = ring.getNode("some-key");
        assertEquals(first, second);
    }

    @Test
    void testDistributionAcrossNodes() {
        ConsistentHashRing<CacheNode> ring = new ConsistentHashRing<>(100);
        ring.addNode(new CacheNode("node-1"));
        ring.addNode(new CacheNode("node-2"));
        ring.addNode(new CacheNode("node-3"));

        Set<String> nodesHit = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            nodesHit.add(ring.getNode("key" + i).getNodeId());
        }
        assertEquals(3, nodesHit.size(), "keys should spread across all nodes with enough virtual nodes");
    }

    @Test
    void testRemoveNode() {
        ConsistentHashRing<CacheNode> ring = new ConsistentHashRing<>(50);
        CacheNode n1 = new CacheNode("node-1");
        ring.addNode(n1);
        assertEquals(50, ring.ringSize());
        ring.removeNode(n1);
        assertEquals(0, ring.ringSize());
    }

    @Test
    void testEmptyRingReturnsNull() {
        ConsistentHashRing<CacheNode> ring = new ConsistentHashRing<>(10);
        assertNull(ring.getNode("anykey"));
    }
}
