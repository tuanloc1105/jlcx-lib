package vn.com.lcx.common.database.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.pool.entry.ConnectionEntry;

import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectionContext {

    private static final ThreadLocal<ConnectionEntry> connetionThreadLocal = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, ConnectionEntry> connectionMap = new ConcurrentHashMap<>();

    public static void set(ConnectionEntry connection) {
        connetionThreadLocal.set(connection);
    }

    public static ConnectionEntry get() {
        return connetionThreadLocal.get();
    }

    public static void clear() {
        connetionThreadLocal.remove();
    }

    public static void set(String key, ConnectionEntry connection) {
        Thread t = Thread.currentThread();
        connectionMap.put(CommonConstant.EMPTY_STRING + t.getId() + key, connection);
    }

    public static ConnectionEntry get(String key) {
        Thread t = Thread.currentThread();
        return connectionMap.get(CommonConstant.EMPTY_STRING + t.getId() + key);
    }

    public static void clear(String key) {
        Thread t = Thread.currentThread();
        connectionMap.remove(CommonConstant.EMPTY_STRING + t.getId() + key);
    }

}
