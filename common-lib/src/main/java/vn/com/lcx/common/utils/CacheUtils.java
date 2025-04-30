package vn.com.lcx.common.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import vn.com.lcx.common.exception.CacheException;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheUtils<K, V> {
    private final int capacity;
    // Use soft reference to ensure memory usage
    private final ConcurrentHashMap<K, SoftReference<V>> cache;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static <K, V> CacheUtils<K, V> create(int capacity) {

        if (capacity < 1) {
            throw new CacheException("Invalid cache capacity");
        }

        return new CacheUtils<>(capacity, new ConcurrentHashMap<>(capacity));
    }

    // Method to add items to the cache
    public void put(K key, V value) {
        if (this.cache.size() >= this.capacity) {
            throw new CacheException("Cache is full");
        }
        this.cache.put(key, new SoftReference<>(value));
    }

    // Method to add items to the cache
    public void put(K key, V value, Duration duration) {
        if (this.cache.size() >= this.capacity) {
            throw new CacheException("Cache is full");
        }
        // new Timer().schedule(
        //         new TimerTask() {
        //             @Override
        //             public void run() {
        //                 CacheUtils.this.remove(key);
        //             }
        //         },
        //         DateTimeUtils.localDateTimeToDate(DateTimeUtils.generateCurrentTimeDefault().plus(duration))
        // );
        this.scheduler.schedule(() -> this.cache.remove(key), duration.toMillis(), TimeUnit.MILLISECONDS);
        this.cache.put(key, new SoftReference<>(value));
    }

    // Method to retrieve items from the cache
    public V get(K key) {
        return this.cache.get(key).get();
    }

    // Method to remove items from the cache
    public void remove(K key) {
        this.cache.remove(key);
    }

    // Method to check if the cache contains a key
    public boolean containsKey(K key) {
        return this.cache.containsKey(key);
    }

    // Method to clear the entire cache
    public void clear() {
        this.cache.clear();
    }
}
