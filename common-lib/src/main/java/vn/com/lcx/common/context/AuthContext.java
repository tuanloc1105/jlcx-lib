package vn.com.lcx.common.context;

public final class AuthContext {

    private AuthContext() {
    }

    private static final ThreadLocal<Object> authThreadLocal = new ThreadLocal<>();

    public static void set(Object connection) {
        authThreadLocal.set(connection);
    }

    public static Object get() {
        return authThreadLocal.get();
    }

    public static <T> T get(Class<T> clz) {
        return clz.cast(authThreadLocal.get());
    }

    public static void clear() {
        authThreadLocal.remove();
    }

}
