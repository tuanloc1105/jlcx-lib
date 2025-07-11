package vn.com.lcx.reactive.cache;

import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisClientType;
import io.vertx.redis.client.RedisOptions;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.PostConstruct;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.constant.CommonConstant;

import java.util.Objects;

import static vn.com.lcx.common.constant.CommonConstant.applicationConfig;

@Component
public class VertxRedisConfiguration {

    private final Vertx vertx;

    public VertxRedisConfiguration(Vertx vertx) {
        this.vertx = vertx;
    }

    @PostConstruct
    public void post() {
        String host = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.redis.host");
        int port;
        try {
            port = Integer.parseInt(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.redis.port"));
        } catch (NumberFormatException e) {
            port = 0;
        }
        String password = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.redis.password");
        int maxPoolSize;
        try {
            maxPoolSize = Integer.parseInt(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.database.max_pool_size"));
        } catch (NumberFormatException e) {
            maxPoolSize = 5;
        }
        if (
                host.equals(CommonConstant.NULL_STRING) ||
                        port == 0 ||
                        maxPoolSize == 0
        ) {
            return;
        }
        ClassPool.setInstance(init(host, port, password, maxPoolSize));
    }

    public Redis init(String host, int port, String password, int maxPoolSize) {
        Objects.requireNonNull(host, "Redis host must be defined");
        if (port < 1) {
            throw new IllegalArgumentException("Redis port must be defined");
        }
        String connectionString;
        if (StringUtils.isBlank(password) || password.equals(CommonConstant.NULL_STRING)) {
            connectionString = "redis://" + host + ":" + port;
        } else {
            connectionString = "redis://:" + password + "@" + host + ":" + port;
        }
        return Redis.createClient(
                vertx,
                new RedisOptions()
                        .setConnectionString(connectionString)
                        .setMaxPoolSize(maxPoolSize == 0 ? 5 : maxPoolSize)
                        .setType(RedisClientType.STANDALONE)
        );
    }
}
