package vn.com.lcx.jpa.context;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class JpaContext {

    private static final ThreadLocal<Map<String, Object>> threadData = ThreadLocal.withInitial(HashMap::new);

    private JpaContext() {
    }

    /**
     * Check if the JPA context is empty or not.
     *
     * @return true if JPA context is empty, false otherwise.
     */
    public static boolean isJpaContextEmpty() {
        return threadData.get().isEmpty();
    }

    /**
     * Put a value into JPA context.
     *
     * @param key   key of the value
     * @param value value to be put
     */
    public static void put(String key, Object value) {
        threadData.get().put(key, value);
    }

    /**
     * Gets a value from the JPA context.
     *
     * @param key key of the value
     * @return the value associated with the given key
     */
    public static Object get(String key) {
        return threadData.get().get(key);
    }

    /**
     * Gets a value from the JPA context with the given class type.
     *
     * @param key   key of the value
     * @param clazz the class type of the value
     * @return the value associated with the given key, casted to the given class type
     */
    public static <T> T get(String key, Class<T> clazz) {
        return clazz.cast(threadData.get().get(key));
    }

    /**
     * Gets a value from the JPA context with the given class type.
     * If the key is not found in the JPA context, returns the given default value.
     *
     * @param key          key of the value
     * @param clazz        the class type of the value
     * @param defaultValue the default value to return if the key is not found
     * @return the value associated with the given key, casted to the given class type
     */
    public static <T> T get(String key, Class<T> clazz, T defaultValue) {
        return clazz.cast(threadData.get().getOrDefault(key, defaultValue));
    }

    /**
     * Removes a value from the JPA context.
     * <p>
     * If the key is not found in the JPA context, this method does nothing.
     *
     * @param key the key of the value to be removed
     */
    public static void remove(String key) {
        threadData.get().remove(key);
    }

    /**
     * Removes all values from the JPA context.
     * <p>
     * This method should be called when the JPA context is no longer needed.
     */
    public static void clearAll() {
        threadData.remove(); // This removes the HashMap instance for the current thread
    }

    /**
     * Gets whether a transaction is currently opened.
     * <p>
     * This can be used to check if a method is currently running inside a transaction.
     * <p>
     * If no transaction is currently opened, returns false.
     *
     * @return true if a transaction is currently opened, false otherwise
     */
    public static boolean isTransactionOpen() {
        return get(JpaConstant.TRANSACTION_IS_OPENED, Boolean.class, false);
    }

    /**
     * Sets whether a transaction is currently opened.
     * <p>
     * This can be used to indicate that a method is currently running inside a transaction.
     * <p>
     * If no transaction is currently opened, sets the value to false.
     *
     * @param value true if a transaction is currently opened, false otherwise
     */
    public static void setTransactionOpen(boolean value) {
        put(JpaConstant.TRANSACTION_IS_OPENED, value);
    }

    /**
     * Gets the transaction isolation.
     * <p>
     * The isolation levels are defined in the JDBC {@link Connection} class.
     * <p>
     * If no value is set, returns {@link Connection#TRANSACTION_READ_COMMITTED}.
     *
     * @return the transaction isolation level
     */
    public static int getTransactionIsolation() {
        return get(JpaConstant.TRANSACTION_ISOLATION, Integer.class, Connection.TRANSACTION_READ_COMMITTED);
    }

    /**
     * Sets the transaction isolation level.
     * <p>
     * The isolation levels are defined in the JDBC {@link Connection} class.
     * <p>
     * If no value is set, {@link Connection#TRANSACTION_READ_COMMITTED} is used.
     *
     * @param value the transaction isolation level
     */
    public static void setTransactionIsolation(int value) {
        put(JpaConstant.TRANSACTION_ISOLATION, value);
    }

    /**
     * Gets the transaction mode.
     * <p>
     * The transaction mode is used to determine how the JPA context handles transactions.
     * <p>
     * The mode can be one of the following values:
     * <ul>
     * <li>{@link JpaConstant#CREATE_NEW_TRANSACTION_MODE}</li>
     * <li>{@link JpaConstant#USE_EXISTING_TRANSACTION_MODE}</li>
     * </ul>
     * <p>
     * If the mode is not set, returns {@link JpaConstant#USE_EXISTING_TRANSACTION_MODE}.
     *
     * @return the transaction mode
     */
    public static int getTransactionMode() {
        return get(JpaConstant.TRANSACTION_MODE, Integer.class, JpaConstant.USE_EXISTING_TRANSACTION_MODE);
    }

    /**
     * Sets the transaction mode.
     * <p>
     * The transaction mode is used to determine how the JPA context handles transactions.
     * <p>
     * The mode can be one of the following values:
     * <ul>
     * <li>{@link JpaConstant#CREATE_NEW_TRANSACTION_MODE}</li>
     * <li>{@link JpaConstant#USE_EXISTING_TRANSACTION_MODE}</li>
     * </ul>
     * <p>
     * If the mode is not set, {@link JpaConstant#USE_EXISTING_TRANSACTION_MODE} is used.
     *
     * @param value the transaction mode
     */
    public static void setTransactionMode(int value) {
        put(JpaConstant.TRANSACTION_MODE, value);
    }

    /**
     * Returns the list of exceptions that should cause a rollback of the transaction when thrown.
     * <p>
     * If no list is set, returns an empty array.
     *
     * @return the list of exceptions that should cause a rollback of the transaction when thrown
     */
    public static Class<?>[] getOnRollbackExceptions() {
        Object obj = get(JpaConstant.TRANSACTION_ON_ROLLBACK);
        if (obj == null) {
            return new Class[0];
        }
        return (Class<?>[]) obj;
    }

    /**
     * Sets the list of exceptions that should cause a rollback of the transaction when thrown.
     * <p>
     * If no list is set, an empty array is used.
     *
     * @param value the list of exceptions that should cause a rollback of the transaction when thrown
     */
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
        put(String.format(JpaConstant.SESSION_KEY_TEMPLATE, EntityContainer.getEntityContainerUUID(entityClass)), session);
    }

    /**
     * Returns the current session being processed.
     *
     * @param entityClass The class is annotated with `jakarta.persistence.Entity`.
     * @return The session is created by the `org.hibernate.SessionFactory` that is managing the `entityClass` you are passing in.
     */
    public static Session getSession(Class<?> entityClass) {
        return get(String.format(JpaConstant.SESSION_KEY_TEMPLATE, EntityContainer.getEntityContainerUUID(entityClass)), Session.class);
    }

    /**
     * Stores the transaction in thread local.
     *
     * @param entityClass The class is annotated with `jakarta.persistence.Entity`.
     * @param session     The transaction you want to store.
     */
    public static void setTransaction(Class<?> entityClass, Transaction session) {
        put(String.format(JpaConstant.TRANSACTION_KEY_TEMPLATE, EntityContainer.getEntityContainerUUID(entityClass)), session);
    }

    /**
     * Returns the current transaction being processed.
     *
     * @param entityClass The class is annotated with `jakarta.persistence.Entity`.
     * @return The transaction is created by the `org.hibernate.SessionFactory` that is managing the `entityClass` you are passing in.
     */
    public static Transaction getTransaction(Class<?> entityClass) {
        return get(String.format(JpaConstant.TRANSACTION_KEY_TEMPLATE, EntityContainer.getEntityContainerUUID(entityClass)), Transaction.class);
    }

    /**
     * Commits all active transactions.
     * <p>
     * If there are transactions that are not active, they will be ignored.
     */
    public static void commit() {
        threadData.get().forEach((key, value) -> {
            if (value instanceof Transaction) {
                if (((Transaction) value).isActive()) {
                    ((Transaction) value).commit();
                }
            }
        });
    }

    /**
     * Rolls back all active transactions.
     * <p>
     * If there are transactions that are not active, they will be ignored.
     */
    public static void rollback() {
        threadData.get().forEach((key, value) -> {
            if (value instanceof Transaction) {
                if (((Transaction) value).isActive()) {
                    ((Transaction) value).rollback();
                }
            }
        });
    }

    /**
     * Closes all active hibernate sessions.
     * <p>
     * If there are sessions that are not active, they will be ignored.
     */
    public static void close() {
        threadData.get().forEach((key, value) -> {
            if (value instanceof Session) {
                if (((Session) value).isOpen()) {
                    ((Session) value).close();
                }
            }
        });
    }

}
