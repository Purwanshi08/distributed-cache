package com.purwa.cache.core;

/**
 * Core contract for a key-value cache with optional TTL support.
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface Cache<K, V> {

    /** Returns the value for {@code key}, or {@code null} if absent or expired. */
    V get(K key);

    /** Inserts/updates {@code key} with no expiry. */
    void put(K key, V value);

    /** Inserts/updates {@code key} with a TTL in milliseconds. Use {@code ttlMillis <= 0} for no expiry. */
    void put(K key, V value, long ttlMillis);

    /** Removes {@code key} if present. */
    void remove(K key);

    /** True if {@code key} is present and not expired. */
    boolean containsKey(K key);

    /** Current number of live entries. */
    int size();

    /** Removes all entries. */
    void clear();
}
