package vn.com.lcx.common.database.pool.entry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.common.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectionEntry implements AutoCloseable {

    @Setter
    private Connection connection;

    @Setter(AccessLevel.PRIVATE)
    private LocalDateTime lastActiveTime;

    @Setter(AccessLevel.PRIVATE)
    private DBTypeEnum dbType;

    @Setter(AccessLevel.PRIVATE)
    private String connectionName;

    @Setter(AccessLevel.PRIVATE)
    private Logger connectionLog;

    private AtomicBoolean idle;

    @Setter
    private boolean criticalLock;


    public static ConnectionEntry init(Connection connection,
                                       DBTypeEnum dbType,
                                       String connectionName) {
        val folder = new File(FileUtils.pathJoining(System.getProperty("java.io.tmpdir"), "lcx-pool"));
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        val file = new File(FileUtils.pathJoining(System.getProperty("java.io.tmpdir"), "lcx-pool", String.format("%s.lock", connectionName)));
        val fileIsNotExist = !file.exists();
        if (fileIsNotExist) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        val logger = LoggerFactory.getLogger(connectionName);
        val entry = new ConnectionEntry(
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

    public void lock() {
        this.idle.set(false);
    }

    public boolean isActive() {
        return !this.idle.get();
    }

    public void releaseLock() {
        this.idle.set(true);
    }

    public void activate() {
        if (this.idle.get()) {
            this.lock();
            this.lastActiveTime = DateTimeUtils.generateCurrentTimeDefault();
            this.idle.set(false);
            this.getConnectionLog().info("Activated connection entry: {}", this);
            return;
        }
        throw new RuntimeException("Connection is not idling");
    }

    public void deactivate() {
        if (this.idle.get()) {
            throw new RuntimeException("Connection is idling");
        }
        this.lastActiveTime = DateTimeUtils.generateCurrentTimeDefault();
        if (transactionIsOpen()) {
            this.commit();
        }
        this.idle.set(true);
        this.releaseLock();
        this.getConnectionLog().info("Deactivated connection entry: {}", this);
    }

    public boolean transactionIsOpen() {
        boolean result = false;
        try {
            result = !this.connection.getAutoCommit();
        } catch (Exception e) {
            this.connectionLog.error(e.getMessage(), e);
        }
        return result;
    }

    public void openTransaction() {
        this.openTransaction(TransactionIsolation.TRANSACTION_READ_COMMITTED);
    }

    public void openTransaction(TransactionIsolation transactionIsolation) {
        try {
            if (this.transactionIsOpen()) {
                return;
            }
            //noinspection MagicConstant
            connection.setTransactionIsolation(transactionIsolation.getValue());
            connection.setAutoCommit(false);
            this.connectionLog.info("Open transaction");
        } catch (SQLException e) {
            this.connectionLog.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

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

    public boolean isValid() {
        try {
            return this.connection.isValid(10);
        } catch (SQLException e) {
            this.connectionLog.error(e.getMessage(), e);
            return false;
        }
    }

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

    @Override
    public void close() {
        this.deactivate();
    }

    @AllArgsConstructor
    @Getter
    public static enum TransactionIsolation {
        TRANSACTION_NONE(Connection.TRANSACTION_NONE),
        TRANSACTION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        TRANSACTION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        TRANSACTION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        TRANSACTION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),
        ;
        private final int value;
    }

}
