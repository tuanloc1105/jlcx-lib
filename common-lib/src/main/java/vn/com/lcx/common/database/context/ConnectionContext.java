package vn.com.lcx.common.database.context;

import vn.com.lcx.common.constant.CommonConstant;

import java.sql.Connection;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated(forRemoval = true)
public class ConnectionContext {

    private static final ThreadLocal<Connection> connetionThreadLocal = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private ConnectionContext() {
    }

    public static void set(Connection connection) {
        connetionThreadLocal.set(connection);
    }

    public static Connection get() {
        return connetionThreadLocal.get();
    }

    public static void clear() {
        connetionThreadLocal.remove();
    }

    public static void set(String key, Connection connection) {
        Thread t = Thread.currentThread();
        connectionMap.put(CommonConstant.EMPTY_STRING + t.getId() + key, connection);
    }

    public static Connection get(String key) {
        Thread t = Thread.currentThread();
        return connectionMap.get(CommonConstant.EMPTY_STRING + t.getId() + key);
    }

    public static void clear(String key) {
        Thread t = Thread.currentThread();
        connectionMap.remove(CommonConstant.EMPTY_STRING + t.getId() + key);
    }

}
