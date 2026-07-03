# Distributed In-Memory Cache (Java)

A thread-safe, sharded in-memory cache library with pluggable eviction
policies (LRU / LFU), TTL expiry, and consistent-hash-based sharding
across multiple in-memory shards — the classic "design a cache" /
"design a distributed cache" system design problem, actually built.

## Why this project

Most fresher LLD portfolios repeat the same handful of projects (parking
lot, elevator, tic-tac-toe). Caching sits at the center of almost every
real backend system (product catalog, session store, rate limiters,
CDN edge caches) and maps directly onto the kind of HLD/LLD interview
question a backend-focused SDE-1 round tends to ask.

## Architecture

```
com.purwa.cache
├── core/               Cache interface, CacheEntry (TTL wrapper),
│                       LRUCache (doubly linked list + hashmap, O(1)),
│                       LFUCache (frequency-bucket technique, O(1))
├── factory/            EvictionPolicyType, CacheFactory (Factory pattern)
├── concurrency/         ThreadSafeCache (Decorator pattern, ReentrantLock)
├── sharding/            ConsistentHashRing, CacheNode, DistributedCache
├── manager/             CacheManager (thread-safe Singleton)
└── Demo.java            runnable end-to-end demo
```

## Design decisions (interview talking points)

**Why manual doubly-linked-list LRU instead of `LinkedHashMap`?**
`LinkedHashMap`'s `accessOrder` mode gets you LRU "for free," but it hides
exactly the mechanics interviewers want to hear you explain — pointer
rewiring on every access, sentinel head/tail nodes to avoid null checks,
O(1) eviction from the tail. Built manually here on purpose.

**Why frequency buckets for LFU, not a heap?**
A heap-based LFU is O(log n) per operation. The frequency-bucket approach
(`Map<frequency, LinkedHashSet<key>>` + a `minFreq` pointer) gets true O(1)
get/put, and it naturally preserves FIFO order for tie-breaking within the
same frequency — which is what most LFU interview follow-ups probe for.

**Why a single `ReentrantLock` in `ThreadSafeCache`, not `ReadWriteLock`?**
This is the one most people get wrong. A `ReadWriteLock` looks like the
obvious choice — "get" seems read-only. But LRU's `get()` mutates the
linked list (moves the node to the front), so two threads calling `get()`
concurrently under a shared read lock could corrupt the list. A single
exclusive lock is the correct, simple answer here. (Real production caches
like Caffeine avoid this bottleneck entirely with lock-free ring buffers
that batch reorder operations — worth mentioning as "further work" if asked
how you'd scale this past a single lock.)

**Why consistent hashing over `hash(key) % numShards`?**
Modulo hashing remaps almost every key when you add/remove a shard —
catastrophic for a live cache (mass cache miss / stampede). Consistent
hashing with virtual nodes means adding/removing a shard only remaps the
keys owned by that node's ring segments, and virtual nodes prevent
lopsided load distribution across physical shards.

## Running it

```bash
# Compile and run the demo (no external deps needed for this part)
mvn compile exec:java -Dexec.mainClass=com.purwa.cache.Demo

# Run the full test suite (JUnit 5, pulled from Maven Central)
mvn test
```

## What's tested

- `LRUCacheTest` — eviction order, recency refresh on update, TTL expiry, invalid capacity
- `LFUCacheTest` — frequency-based eviction, FIFO tie-breaking, TTL expiry
- `ConsistentHashRingTest` — deterministic routing, distribution across nodes, node removal
- `DistributedCacheTest` — cross-shard put/get correctness, stable key routing, runtime shard addition
- `ConcurrencyTest` — 20 threads × 500 ops each against a shared cache, asserting no deadlock and capacity is never exceeded

## Possible extensions (if you want to go further before submitting)

- Write-through / write-back persistence to disk or a real DB
- Cache stampede protection (single-flight pattern for concurrent misses on the same key)
- Metrics: hit rate, eviction count, per-shard load (good for a "how would you monitor this in prod" follow-up)
