package vn.com.lcx.common.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import vn.com.lcx.common.exception.CacheException;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheUtils<K, V> {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int capacity;
    // Use soft reference to ensure memory usage
    private final ConcurrentHashMap<K, SoftReference<V>> cache;
    private final ConcurrentHashMap<K, ScheduledFuture<SoftReference<V>>> removeExpiredKeyTasks = new ConcurrentHashMap<>();

    public static <K, V> CacheUtils<K, V> create(int capacity) {

        if (capacity < 1) {
            throw new CacheException("Invalid cache capacity");
        }

        return new CacheUtils<>(capacity, new ConcurrentHashMap<>(capacity));
    }

    // Method to add items to the cache
    public void put(K key, V value) {
        if (cache.size() >= capacity) {
            throw new CacheException("Cache is full");
        }
        cache.put(key, new SoftReference<>(value));
    }

    // Method to add items to the cache
    public void put(K key, V value, Duration duration) {
        if (cache.size() >= capacity) {
            throw new CacheException("Cache is full");
        }

        cancelOldTask(key);

        cache.put(key, new SoftReference<>(value));

        final ScheduledFuture<SoftReference<V>> schedule = scheduler.schedule(
                () -> {
                    removeExpiredKeyTasks.remove(key);
                    return cache.remove(key);
                },
                duration.toMillis(),
                TimeUnit.MILLISECONDS
        );
        removeExpiredKeyTasks.put(key, schedule);
    }

    // Method to retrieve items from the cache
    public V get(K key) {
        final var ref = cache.get(key);
        return ref == null ? null : ref.get();
    }

    // Method to remove items from the cache
    public void remove(K key) {
        cache.remove(key);
        cancelOldTask(key);
    }

    // Method to check if the cache contains a key
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    // Method to clear the entire cache
    public void clear() {
        cache.clear();
        for (Map.Entry<K, ScheduledFuture<SoftReference<V>>> entry : removeExpiredKeyTasks.entrySet()) {
            ScheduledFuture<SoftReference<V>> task = entry.getValue();
            task.cancel(false);
        }
        removeExpiredKeyTasks.clear();
    }

    private void cancelOldTask(K key) {
        final var oldTask = removeExpiredKeyTasks.remove(key);
        if (oldTask != null) {
            oldTask.cancel(false);
        }
    }
}
