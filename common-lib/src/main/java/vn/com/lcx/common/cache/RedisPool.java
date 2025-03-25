package vn.com.lcx.common.cache;

public interface RedisPool extends AutoCloseable {
    void ping();

    boolean set(String key, String value, int expireSecond);

    boolean delete(String key);

    String get(String key);
}
