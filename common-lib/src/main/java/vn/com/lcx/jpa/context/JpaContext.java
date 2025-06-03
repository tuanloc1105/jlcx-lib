package vn.com.lcx.jpa.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JpaContext {

    private static final ThreadLocal<Map<String, Object>> threadData =
            ThreadLocal.withInitial(HashMap::new);

    public static boolean isJpaContextEmpty() {
        return threadData.get().isEmpty();
    }

    public static void put(String key, Object value) {
        threadData.get().put(key, value);
    }

    public static Object get(String key) {
        return threadData.get().get(key);
    }

    public static <T> T get(String key, Class<T> clazz) {
        return clazz.cast(threadData.get().get(key));
    }

    public static <T> T get(String key, Class<T> clazz, T defaultValue) {
        return clazz.cast(threadData.get().getOrDefault(key, defaultValue));
    }

    public static void remove(String key) {
        threadData.get().remove(key);
    }

    public static void clearAll() {
        threadData.remove(); // This removes the HashMap instance for the current thread
    }

    public static boolean isTransactionOpen() {
        return get(JpaConstant.TRANSACTION_IS_OPENED, Boolean.class, false);
    }

    public static void setTransactionOpen(boolean value) {
        put(JpaConstant.TRANSACTION_IS_OPENED, value);
    }

    public static int getTransactionIsolation() {
        return get(JpaConstant.TRANSACTION_ISOLATION, Integer.class, Connection.TRANSACTION_READ_COMMITTED);
    }

    public static void setTransactionIsolation(int value) {
        put(JpaConstant.TRANSACTION_ISOLATION, value);
    }

    public static int getTransactionMode() {
        return get(JpaConstant.TRANSACTION_MODE, Integer.class, JpaConstant.USE_EXISTING_TRANSACTION_MODE);
    }

    public static void setTransactionMode(int value) {
        put(JpaConstant.TRANSACTION_MODE, value);
    }

    public static Class<?>[] getOnRollbackExceptions() {
        Object obj = get(JpaConstant.TRANSACTION_ON_ROLLBACK);
        if (obj == null) {
            return new Class[0];
        }
        return (Class<?>[]) obj;
    }

    public static void setOnRollbackExceptions(Class<? extends Throwable>[] value) {
        put(JpaConstant.TRANSACTION_ON_ROLLBACK, value);
    }

    /**
     * Stores the session in thread local.
     *
     * @param entityClass The class is annotated with `jakarta.persistence.Entity`.
     * @param session     The session you want to store.
     */
    public static void setSession(Class<?> entityClass, Session session) {
        put(
                String.format(
                        JpaConstant.SESSION_KEY_TEMPLATE,
                        EntityContainer.getEntityContainerUUID(entityClass)
                ),
                session
        );
    }

    /**
     * Returns the current session being processed.
     *
     * @param entityClass The class is annotated with `jakarta.persistence.Entity`.
     * @return The session is created by the `org.hibernate.SessionFactory` that is managing the `entityClass` you are passing in.
     */
    public static Session getSession(Class<?> entityClass) {
        return get(
                String.format(
                        JpaConstant.SESSION_KEY_TEMPLATE,
                        EntityContainer.getEntityContainerUUID(entityClass)
                ),
                Session.class
        );
    }

    public static void setTransaction(Class<?> entityClass, Transaction session) {
        put(
                String.format(
                        JpaConstant.TRANSACTION_KEY_TEMPLATE,
                        EntityContainer.getEntityContainerUUID(entityClass)
                ),
                session
        );
    }

    public static Transaction getTransaction(Class<?> entityClass) {
        return get(
                String.format(
                        JpaConstant.TRANSACTION_KEY_TEMPLATE,
                        EntityContainer.getEntityContainerUUID(entityClass)
                ),
                Transaction.class
        );
    }

    public static void commit() {
        threadData.get().forEach((key, value) -> {
            if (value instanceof Transaction) {
                if (((Transaction) value).isActive()) {
                    ((Transaction) value).commit();
                }
            }
        });
    }

    public static void rollback() {
        threadData.get().forEach((key, value) -> {
            if (value instanceof Transaction) {
                if (((Transaction) value).isActive()) {
                    ((Transaction) value).rollback();
                }
            }
        });
    }

    public static void close() {
        threadData.get().forEach((key, value) -> {
            if (value instanceof Session) {
                ((Session) value).close();
            }
        });
    }

}
