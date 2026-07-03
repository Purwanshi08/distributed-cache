package com.purwa.cache.concurrency;

import com.purwa.cache.core.Cache;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Decorator pattern: wraps any {@link Cache} to make it thread-safe.
 *
 * Deliberately uses a single {@link ReentrantLock} rather than a
 * ReadWriteLock. A read/write split looks attractive at first glance, but
 * LRU's get() mutates internal state (it moves the accessed node to the
 * front of the list) — so "get" is NOT a pure read for this data structure,
 * and using a shared read lock for it would allow two threads to corrupt
 * the linked list concurrently. This is a good interview talking point:
 * production caches (e.g. Caffeine) solve this with lock-free ring buffers
 * that batch reorder operations instead of mutating on every read.
 */
public class ThreadSafeCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> delegate;
    private final ReentrantLock lock = new ReentrantLock();

    public ThreadSafeCache(Cache<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public V get(K key) {
        lock.lock();
        try {
            return delegate.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        put(key, value, -1);
    }

    @Override
    public void put(K key, V value, long ttlMillis) {
        lock.lock();
        try {
            delegate.put(key, value, ttlMillis);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        lock.lock();
        try {
            delegate.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        lock.lock();
        try {
            return delegate.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return delegate.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            delegate.clear();
        } finally {
            lock.unlock();
        }
    }
}
