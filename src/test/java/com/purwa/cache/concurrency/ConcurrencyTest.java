package com.purwa.cache.concurrency;

import com.purwa.cache.core.Cache;
import com.purwa.cache.core.LRUCache;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrencyTest {

    @Test
    void testConcurrentPutsGetsDoNotCorruptStateOrDeadlock() throws InterruptedException {
        Cache<Integer, Integer> cache = new ThreadSafeCache<>(new LRUCache<>(1000));
        int threadCount = 20;
        int opsPerThread = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        int key = threadId * opsPerThread + i;
                        cache.put(key, key);
                        cache.get(key);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "all threads should complete without deadlock");
        assertTrue(cache.size() <= 1000, "cache should never exceed its configured capacity");
    }
}
