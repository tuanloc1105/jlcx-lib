package vn.com.lcx.common.cache;

import vn.com.lcx.common.exception.CacheException;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread-safe cache implementation with soft references and TTL support.
 *
 * <p>This cache uses {@link SoftReference} to allow garbage collection when memory is low,
 * and supports time-based expiration for cached entries.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Thread-safe operations using {@link ConcurrentHashMap}</li>
 *   <li>Soft references to allow GC when memory pressure is high</li>
 *   <li>Time-based expiration (TTL) support</li>
 *   <li>LRU eviction when cache reaches capacity</li>
 *   <li>Automatic cleanup of expired entries</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * // Create a cache with capacity 1000
 * CacheUtils<String, User> cache = CacheUtils.create(1000);
 *
 * // Add entry without expiration
 * cache.put("user1", user1);
 *
 * // Add entry with 5 minutes expiration
 * cache.put("user2", user2, Duration.ofMinutes(5));
 *
 * // Retrieve entry
 * User user = cache.get("user1");
 *
 * // Check if key exists
 * if (cache.containsKey("user1")) {
 *     // do something
 * }
 *
 * // Remove entry
 * cache.remove("user1");
 *
 * // Clear all entries
 * cache.clear();
 *
 * // Shutdown when done (important for cleanup)
 * cache.shutdown();
 * }</pre>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @author LCX Team
 * @since 1.0.0
 */
public class CacheUtils<K, V> {

    /**
     * Scheduler for handling TTL expiration
     */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Maximum number of entries in the cache
     */
    private final int capacity;

    /**
     * Main cache storage using soft references
     */
    private final ConcurrentHashMap<K, SoftReference<V>> cache;

    /**
     * Tasks for removing expired entries
     */
    private final ConcurrentHashMap<K, ScheduledFuture<SoftReference<V>>> removeExpiredKeyTasks = new ConcurrentHashMap<>();

    /**
     * Counter for tracking cache size (atomic for thread safety)
     */
    private final AtomicInteger size = new AtomicInteger(0);

    private CacheUtils(int capacity, ConcurrentHashMap<K, SoftReference<V>> cache) {
        this.capacity = capacity;
        this.cache = cache;
    }

    /**
     * Creates a new cache instance with the specified capacity.
     *
     * @param capacity the maximum number of entries the cache can hold
     * @param <K>      the type of keys
     * @param <V>      the type of values
     * @return a new CacheUtils instance
     * @throws CacheException if capacity is less than 1
     */
    public static <K, V> CacheUtils<K, V> create(int capacity) {
        if (capacity < 1) {
            throw new CacheException("Cache capacity must be greater than 0, got: " + capacity);
        }
        return new CacheUtils<>(capacity, new ConcurrentHashMap<>(capacity));
    }

    /**
     * Shuts down the scheduler used for TTL expiration.
     * This method should be called when the cache is no longer needed to prevent memory leaks.
     *
     * <p>Note: This is a static method that affects all cache instances since they share the same scheduler.</p>
     */
    public static void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Adds an entry to the cache without expiration.
     * If the cache is full, the least recently used entry will be evicted.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @throws IllegalArgumentException if key or value is null
     */
    public void put(K key, V value) {
        validateKeyValue(key, value);
        evictIfNeeded();
        cache.put(key, new SoftReference<>(value));
        size.incrementAndGet();
    }

    /**
     * Adds an entry to the cache with time-based expiration.
     * If the cache is full, the least recently used entry will be evicted.
     *
     * @param key      the key with which the specified value is to be associated
     * @param value    the value to be associated with the specified key
     * @param duration the time duration after which the entry will expire
     * @throws IllegalArgumentException if key, value, or duration is null, or if duration is negative
     */
    public void put(K key, V value, Duration duration) {
        validateKeyValue(key, value);
        if (duration == null) {
            throw new IllegalArgumentException("Duration cannot be null");
        }
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Duration cannot be negative: " + duration);
        }

        evictIfNeeded();
        cancelOldTask(key);

        cache.put(key, new SoftReference<>(value));
        size.incrementAndGet();

        final ScheduledFuture<SoftReference<V>> schedule = scheduler.schedule(
                () -> {
                    removeExpiredKeyTasks.remove(key);
                    SoftReference<V> removed = cache.remove(key);
                    if (removed != null) {
                        size.decrementAndGet();
                    }
                    return removed;
                },
                duration.toMillis(),
                TimeUnit.MILLISECONDS
        );
        removeExpiredKeyTasks.put(key, schedule);
    }

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if the key is not present
     */
    public V get(K key) {
        if (key == null) {
            return null;
        }
        final SoftReference<V> ref = cache.get(key);
        if (ref == null) {
            return null;
        }
        V value = ref.get();
        if (value == null) {
            // Value has been garbage collected, remove the entry
            cache.remove(key);
            size.decrementAndGet();
        }
        return value;
    }

    /**
     * Removes the mapping for the specified key from the cache.
     *
     * @param key the key whose mapping is to be removed
     */
    public void remove(K key) {
        if (key == null) {
            return;
        }
        SoftReference<V> removed = cache.remove(key);
        if (removed != null) {
            size.decrementAndGet();
        }
        cancelOldTask(key);
    }

    /**
     * Returns true if the cache contains a mapping for the specified key.
     *
     * @param key the key whose presence in the cache is to be tested
     * @return true if the cache contains a mapping for the specified key
     */
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }
        SoftReference<V> ref = cache.get(key);
        if (ref == null) {
            return false;
        }
        V value = ref.get();
        if (value == null) {
            // Value has been garbage collected, remove the entry
            cache.remove(key);
            size.decrementAndGet();
            return false;
        }
        return true;
    }

    /**
     * Returns the number of entries in the cache.
     *
     * @return the number of entries in the cache
     */
    public int size() {
        return size.get();
    }

    /**
     * Returns true if the cache contains no entries.
     *
     * @return true if the cache contains no entries
     */
    public boolean isEmpty() {
        return size.get() == 0;
    }

    /**
     * Removes all entries from the cache and cancels all scheduled expiration tasks.
     */
    public void clear() {
        cache.clear();
        size.set(0);
        for (Map.Entry<K, ScheduledFuture<SoftReference<V>>> entry : removeExpiredKeyTasks.entrySet()) {
            ScheduledFuture<SoftReference<V>> task = entry.getValue();
            if (task != null) {
                task.cancel(false);
            }
        }
        removeExpiredKeyTasks.clear();
    }

    /**
     * Evicts the least recently used entry if the cache is at capacity.
     * This is a simple implementation that removes the first entry found.
     * For better LRU implementation, consider using LinkedHashMap or a custom LRU structure.
     */
    private void evictIfNeeded() {
        if (size.get() >= capacity) {
            // Simple eviction strategy: remove the first entry
            for (Map.Entry<K, SoftReference<V>> entry : cache.entrySet()) {
                K key = entry.getKey();
                SoftReference<V> ref = entry.getValue();
                if (ref.get() == null) {
                    // Already garbage collected, remove it
                    cache.remove(key);
                    size.decrementAndGet();
                    cancelOldTask(key);
                    return;
                }
            }

            // If no garbage collected entries found, remove the first one
            Map.Entry<K, SoftReference<V>> firstEntry = cache.entrySet().iterator().next();
            if (firstEntry != null) {
                cache.remove(firstEntry.getKey());
                size.decrementAndGet();
                cancelOldTask(firstEntry.getKey());
            }
        }
    }

    /**
     * Validates that key and value are not null.
     *
     * @param key   the key to validate
     * @param value the value to validate
     * @throws IllegalArgumentException if key or value is null
     */
    private void validateKeyValue(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Cache value cannot be null");
        }
    }

    /**
     * Cancels the scheduled task for the specified key.
     *
     * @param key the key whose scheduled task should be cancelled
     */
    private void cancelOldTask(K key) {
        final ScheduledFuture<SoftReference<V>> oldTask = removeExpiredKeyTasks.remove(key);
        if (oldTask != null) {
            oldTask.cancel(false);
        }
    }
}
