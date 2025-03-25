package vn.com.lcx.common.cache;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import vn.com.lcx.common.utils.LogUtils;

public class RedisPoolImpl implements RedisPool {

    private final JedisPool jedisPool;

    private RedisPoolImpl(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public static RedisPoolImpl create(String host, int port, String password) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(5);
        config.setMaxTotal(10);
        config.setMinIdle(1);
        JedisPool jedisPool;
        if (StringUtils.isNotBlank(password)) {
            jedisPool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT, password);
        } else {
            jedisPool = new JedisPool(config, host, port);
        }
        return new RedisPoolImpl(jedisPool);
    }

    @Override
    public void ping() {
        try (Jedis jedis = jedisPool.getResource()) {
            val redisPingResult = jedis.ping();
            LogUtils.writeLog(LogUtils.Level.INFO, redisPingResult);
        }
    }

    @Override
    public boolean set(String key, String value, int expireSecond) {
        boolean success = false;
        try (Jedis jedis = jedisPool.getResource()) {
            val setValueForKeyResult = jedis.set(key, value);
            if (!"OK".equals(setValueForKeyResult)) {
                return success;
            }
            LogUtils.writeLog(
                    LogUtils.Level.INFO,
                    "Save data into redis:\n    - Key name: {}\n    - Value: {}",
                    key,
                    value
            );
            success = true;
            if (expireSecond > 0) {
                val setExpireResult = jedis.expire(key, expireSecond);
                if (setExpireResult == 0) {
                    LogUtils.writeLog(LogUtils.Level.WARN, "Cannot set expire time for key {}", key);
                } else {
                    LogUtils.writeLog(
                            LogUtils.Level.INFO,
                            "Key {} will expire after {} second(s)",
                            key,
                            expireSecond
                    );
                }
            }
        }
        return success;
    }

    @Override
    public boolean delete(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            val redisDeleteKeyResult = jedis.del(key);
            return redisDeleteKeyResult == 1;
        }
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public void close() {
        this.jedisPool.close();
    }
}
