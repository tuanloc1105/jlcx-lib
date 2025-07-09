package vn.com.lcx.common.database.pool.entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.common.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a database connection entry in the connection pool.
 * This class manages the lifecycle of a database connection including activation, deactivation,
 * transaction management, and resource cleanup.
 *
 * <p>Implements {@link AutoCloseable} to ensure proper resource cleanup using try-with-resources.</p>
 *
 * @see AutoCloseable
 */
public final class ConnectionEntry implements AutoCloseable {

    /**
     * The underlying database connection.
     */
    private Connection connection;

    /**
     * The timestamp when this connection was last active.
     */
    private LocalDateTime lastActiveTime;

    /**
     * The type of database this connection is associated with.
     */
    private DBTypeEnum dbType;

    /**
     * The unique name identifying this connection.
     */
    private String connectionName;

    /**
     * Logger instance for this connection entry.
     */
    private Logger connectionLog;

    /**
     * Atomic flag indicating whether this connection is currently idle.
     */
    private AtomicBoolean idle;

    /**
     * Flag indicating if this connection is under a critical section.
     */
    private boolean criticalLock;

    private ConnectionEntry() {
    }

    private ConnectionEntry(Connection connection,
                            LocalDateTime lastActiveTime,
                            DBTypeEnum dbType,
                            String connectionName,
                            Logger connectionLog,
                            AtomicBoolean idle,
                            boolean criticalLock) {
        this.connection = connection;
        this.lastActiveTime = lastActiveTime;
        this.dbType = dbType;
        this.connectionName = connectionName;
        this.connectionLog = connectionLog;
        this.idle = idle;
        this.criticalLock = criticalLock;
    }

    /**
     * Initializes a new ConnectionEntry with the specified connection details.
     *
     * @param connection     the database connection to be managed
     * @param dbType         the type of the database
     * @param connectionName a unique name for the connection
     * @return a new initialized ConnectionEntry
     * @throws RuntimeException if there's an error creating the connection lock file
     */
    public static ConnectionEntry init(Connection connection,
                                       DBTypeEnum dbType,
                                       String connectionName) {
        final var folder = new File(FileUtils.pathJoining(System.getProperty("java.io.tmpdir"), "lcx-pool"));
        // noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        final var file = new File(FileUtils.pathJoining(System.getProperty("java.io.tmpdir"), "lcx-pool", String.format("%s.lock", connectionName)));
        final var fileIsNotExist = !file.exists();
        if (fileIsNotExist) {
            try {
                // noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        final var logger = LoggerFactory.getLogger(connectionName);
        final var entry = new ConnectionEntry(
                connection,
                DateTimeUtils.generateCurrentTimeDefault(),
                dbType,
                connectionName,
                logger,
                new AtomicBoolean(true),
                false
        );

        logger.info("Add new connection entry: {}", entry);
        return entry;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    private void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public DBTypeEnum getDbType() {
        return dbType;
    }

    private void setDbType(DBTypeEnum dbType) {
        this.dbType = dbType;
    }

    public String getConnectionName() {
        return connectionName;
    }

    private void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public Logger getConnectionLog() {
        return connectionLog;
    }

    private void setConnectionLog(Logger connectionLog) {
        this.connectionLog = connectionLog;
    }

    public AtomicBoolean getIdle() {
        return idle;
    }

    public boolean isCriticalLock() {
        return criticalLock;
    }

    public void setCriticalLock(boolean criticalLock) {
        this.criticalLock = criticalLock;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionEntry that = (ConnectionEntry) o;
        return isCriticalLock() == that.isCriticalLock() &&
                Objects.equals(getConnection(), that.getConnection()) &&
                Objects.equals(getLastActiveTime(), that.getLastActiveTime()) &&
                getDbType() == that.getDbType() &&
                Objects.equals(getConnectionName(), that.getConnectionName()) &&
                Objects.equals(getConnectionLog(), that.getConnectionLog());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConnection(), getLastActiveTime(), getDbType(), getConnectionName(), getConnectionLog(), getIdle(), isCriticalLock());
    }

    @Override
    public String toString() {
        return "ConnectionEntry{" + "connection=" + connection +
                ", lastActiveTime=" + lastActiveTime +
                ", dbType=" + dbType +
                ", connectionName='" + connectionName + '\'' +
                ", idle=" + idle +
                ", criticalLock=" + criticalLock +
                '}';
    }

    /**
     * Locks this connection, marking it as in-use.
     */
    public void lock() {
        this.idle.set(false);
    }

    /**
     * Checks if this connection is currently active (in use).
     *
     * @return true if the connection is active, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isActive() {
        return !this.idle.get();
    }

    /**
     * Releases the lock on this connection, marking it as idle.
     */
    public void releaseLock() {
        this.idle.set(true);
    }

    /**
     * Activates this connection entry for use.
     *
     * @throws RuntimeException if the connection is not idle
     */
    public void activate() {
        // if (this.idle.get()) {
        //     this.lock();
        //     this.lastActiveTime = DateTimeUtils.generateCurrentTimeDefault();
        //     this.getConnectionLog().info("Activated connection entry: {}", this);
        //     return;
        // }
        // throw new RuntimeException("Connection is not idling");
        if (this.idle.compareAndSet(true, false)) {
            this.lastActiveTime = DateTimeUtils.generateCurrentTimeDefault();
            this.connectionLog.info("Activated connection entry: {}", this);
        } else {
            throw new RuntimeException("Connection is not idling");
        }
    }

    /**
     * Deactivates this connection entry, committing any open transactions
     * and releasing the lock.
     *
     * @throws RuntimeException if the connection is already idle
     */
    public void deactivate() {
        if (this.idle.get()) {
            throw new RuntimeException("Connection is idling");
        }
        this.lastActiveTime = DateTimeUtils.generateCurrentTimeDefault();
        if (transactionIsOpen()) {
            this.commit();
        }
        this.releaseLock();
        this.getConnectionLog().info("Deactivated connection entry: {}", this);
    }

    /**
     * Checks if there is an open transaction on this connection.
     *
     * @return true if there is an open transaction, false otherwise
     */
    public boolean transactionIsOpen() {
        boolean result = false;
        try {
            result = !this.connection.getAutoCommit();
        } catch (Exception e) {
            this.connectionLog.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Opens a new transaction with READ_COMMITTED isolation level.
     * Does nothing if a transaction is already open.
     */
    public void openTransaction() {
        this.openTransaction(TransactionIsolation.TRANSACTION_READ_COMMITTED);
    }

    /**
     * Opens a new transaction with the specified isolation level.
     * Does nothing if a transaction is already open.
     *
     * @param transactionIsolation the isolation level for the transaction
     * @throws RuntimeException if there's a database error
     */
    public void openTransaction(TransactionIsolation transactionIsolation) {
        try {
            if (this.transactionIsOpen()) {
                return;
            }
            // noinspection MagicConstant
            connection.setTransactionIsolation(transactionIsolation.getValue());
            connection.setAutoCommit(false);
            this.connectionLog.info("Open transaction");
        } catch (SQLException e) {
            this.connectionLog.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Commits the current transaction and enables auto-commit mode.
     * Does nothing if no transaction is open.
     *
     * @throws RuntimeException if there's a database error
     */
    public void commit() {
        try {
            if (this.transactionIsOpen()) {
                this.connection.commit();
                this.connection.setAutoCommit(true);
                this.connectionLog.info("Committed");
            }
        } catch (SQLException e) {
            this.connectionLog.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Commits the current transaction without enabling auto-commit mode.
     * Does nothing if no transaction is open.
     *
     * @throws RuntimeException if there's a database error
     */
    public void commitNoClose() {
        try {
            if (this.transactionIsOpen()) {
                this.connection.commit();
                this.connectionLog.info("Committed but not closing");
            }
        } catch (SQLException e) {
            this.connectionLog.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Rolls back the current transaction and enables auto-commit mode.
     * Does nothing if no transaction is open.
     *
     * @throws RuntimeException if there's a database error
     */
    public void rollback() {
        try {
            if (this.transactionIsOpen()) {
                this.connection.rollback();
                this.connection.setAutoCommit(true);
                this.connectionLog.info("Rolled back");
            }
        } catch (SQLException e) {
            this.connectionLog.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates that the connection is still valid.
     *
     * @return true if the connection is valid, false otherwise
     */
    public boolean isValid() {
        try {
            return this.connection.isValid(10);
        } catch (SQLException e) {
            this.connectionLog.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Shuts down this connection entry, closing the underlying connection
     * and releasing all resources.
     */
    public void shutdown() {
        try {
            if (!this.connection.isClosed() && this.isValid()) {
                this.connection.close();
            }
        } catch (SQLException e) {
            this.connectionLog.error(e.getMessage(), e);
        }
        this.connection = null;
    }

    /**
     * Releases this connection back to the pool by deactivating it.
     * Implements {@link AutoCloseable#close()} for try-with-resources support.
     */
    @Override
    public void close() {
        this.deactivate();
    }

    /**
     * Enum representing standard JDBC transaction isolation levels.
     */
    public static enum TransactionIsolation {
        TRANSACTION_NONE(Connection.TRANSACTION_NONE),
        TRANSACTION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        TRANSACTION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        TRANSACTION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        TRANSACTION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),
        ;
        /**
         * The JDBC constant value for this isolation level.
         */
        private final int value;

        TransactionIsolation(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
