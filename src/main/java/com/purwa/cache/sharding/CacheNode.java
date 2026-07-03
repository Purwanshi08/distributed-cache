package com.purwa.cache.sharding;

/** Identifies a single cache shard on the consistent hash ring. */
public class CacheNode {

    private final String nodeId;

    public CacheNode(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    @Override
    public String toString() {
        return nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheNode)) return false;
        return nodeId.equals(((CacheNode) o).nodeId);
    }

    @Override
    public int hashCode() {
        return nodeId.hashCode();
    }
}
