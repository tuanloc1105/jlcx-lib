package vn.com.lcx.common.database.pool;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.database.DatabaseProperty;
import vn.com.lcx.common.database.pool.entry.ConnectionEntry;
import vn.com.lcx.common.database.pool.wrapper.LCXConnection;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.exception.LCXDataSourceException;
import vn.com.lcx.common.exception.LCXDataSourcePropertiesException;
import vn.com.lcx.common.thread.RejectMode;
import vn.com.lcx.common.thread.SimpleExecutor;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.common.utils.LogUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static vn.com.lcx.common.utils.RandomUtils.generateRandomString;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LCXDataSource {

    private final String poolName;
    private final String defaultDriverClassName;
    private final String showDbVersionSqlStatement;
    private final DatabaseProperty property;
    private final SimpleExecutor<Boolean> myExecutor;
    @Getter
    private final DBTypeEnum dbType;
    private final int maxPoolWaitingTime;
    @Getter(AccessLevel.PRIVATE)
    private final ConcurrentLinkedQueue<ConnectionEntry> pool;

    // private final transient Object lock = new Object();

    private static Connection createConnection(String url, String user, String password, int maxTimeoutSecond) throws SQLException, ClassNotFoundException {
        DriverManager.setLoginTimeout(maxTimeoutSecond);
        return DriverManager.getConnection(url, user, password);
    }

    public static LCXDataSource init(String databaseHost,
                                     int databasePort,
                                     String username,
                                     String password,
                                     String databaseName,
                                     String driverClassName,
                                     int initialPoolSize,
                                     int maxPoolSize,
                                     int maxTimeout,
                                     DBTypeEnum dbType) {
        try {
            final String connectionString = String.format(dbType.getTemplateUrlConnectionString(), databaseHost, databasePort, databaseName);
            DatabaseProperty property = new DatabaseProperty(
                    connectionString,
                    username,
                    password,
                    driverClassName,
                    initialPoolSize,
                    maxPoolSize,
                    maxTimeout,
                    true,
                    true
            );
            if (property.propertiesIsAllSet()) {
                val simpleExecutor = SimpleExecutor.<Boolean>init(
                        initialPoolSize,
                        initialPoolSize,
                        RejectMode.ABORT_POLICY,
                        maxTimeout,
                        TimeUnit.SECONDS
                );
                val lcxPool = new LCXDataSource(
                        databaseName,
                        property.getDriverClassName(),
                        dbType.getShowDbVersionSqlStatement(),
                        property,
                        simpleExecutor,
                        dbType,
                        30_000,
                        new ConcurrentLinkedQueue<>()
                );
                lcxPool.create(
                        property.getConnectionString(),
                        property.getUsername(),
                        property.getPassword(),
                        property.getDriverClassName(),
                        maxTimeout
                );
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    LoggerFactory.getLogger(databaseName).info("Shutting down pool");
                    for (ConnectionEntry entry : lcxPool.getPool()) {
                        try {
                            if (entry.transactionIsOpen()) {
                                entry.commit();
                            }
                            entry.shutdown();
                        } catch (Exception e) {
                            entry.getConnectionLog().error("Error closing connection {}", entry, e);
                        }
                    }
                }));
                return lcxPool;
            }
            throw new LCXDataSourcePropertiesException("Database properties is not all set");
        } catch (Exception e) {
            LogUtils.writeLog2(e.getMessage(), e);
            throw new LCXDataSourceException(e);
        }
    }

    private void create(String url, String user, String password, String driverClassName, int maxTimeoutSecond) throws SQLException, ClassNotFoundException {
        if (StringUtils.isBlank(driverClassName)) {
            LogUtils.writeLog2(LogUtils.Level.INFO, "Driver class name is not provided, using default {}", defaultDriverClassName);
            Class.forName(defaultDriverClassName);
        } else {
            LogUtils.writeLog2(LogUtils.Level.INFO, "Using driver class name {}", driverClassName);
            Class.forName(driverClassName);
        }
        LogUtils.writeLog2(LogUtils.Level.INFO, "Creating new connection at url {}", url);
        List<ConnectionEntry> pool = new ArrayList<>(this.property.getInitialPoolSize());
        for (int i = 0; i < this.property.getInitialPoolSize(); i++) {
            this.myExecutor.addNewTask(
                    () -> {
                        Thread.currentThread().setName(Thread.currentThread().getName().replace("pool", "lcx"));
                        val entry = ConnectionEntry.init(
                                createConnection(url, user, password, maxTimeoutSecond),
                                this.dbType,
                                String.format("%s-%s", this.dbType.name().toLowerCase(), generateRandomString(8))
                        );
                        return pool.add(entry);
                    }
            );
        }
        this.myExecutor.executeTasksWithCountDownLatch();
        this.pool.addAll(pool);
    }

    /**
     * @deprecated this method will not be removed but I no longer maintain this method anymore
     * @return
     */
    @Deprecated
    public ConnectionEntry getConnection() {
        LogUtils.writeLog2(
                LogUtils.Level.INFO,
                String.format(
                        "Pool status:\n    - Total connections:   %d\n    - Active connections:  %d\n    - Idle connections:    %d",
                        this.getTotalConnections(),
                        this.getActiveConnections(),
                        this.getIdleConnections()
                )
        );
        try {
            for (ConnectionEntry connectionEntry : this.pool) {
                if (!connectionEntry.isActive() && !connectionEntry.isCriticalLock()) {
                    connectionEntry.activate();
                    if (connectionEntry.isValid()) {
                        connectionEntry.getConnectionLog().info("Connection is valid");
                        return connectionEntry;
                    }
                    connectionEntry.setConnection(
                            createConnection(
                                    this.property.getConnectionString(),
                                    this.property.getUsername(),
                                    this.property.getPassword(),
                                    this.property.getMaxTimeout()
                            )
                    );
                    connectionEntry.getConnectionLog().info("Recreating connection");
                    return connectionEntry;
                }
            }
            if (this.pool.size() < this.property.getMaxPoolSize()) {
                val entry = ConnectionEntry.init(
                        createConnection(
                                this.property.getConnectionString(),
                                this.property.getUsername(),
                                this.property.getPassword(),
                                this.property.getMaxTimeout()
                        ),
                        this.dbType,
                        String.format("%s-%s", this.dbType.name().toLowerCase(), generateRandomString(8))
                );
                this.pool.add(entry);
                entry.activate();
                return entry;
            }
            LogUtils.writeLog(LogUtils.Level.INFO, "All connections in pool are being used");
            long startTime = System.currentTimeMillis();
            long waitedTime = System.currentTimeMillis() - startTime;
            while (waitedTime < maxPoolWaitingTime) {
                waitedTime = System.currentTimeMillis() - startTime;
                LogUtils.writeLog(LogUtils.Level.INFO, "Waited {} ms", waitedTime);
                val currentTime = DateTimeUtils.generateCurrentTimeDefault();
                val entry = this.pool.stream()
                        .sorted(Comparator.comparing(ConnectionEntry::getLastActiveTime))
                        .filter(e -> {
                            val durationBetweenLastTimeActiveAndCurrentTime = Duration.between(e.getLastActiveTime(), currentTime);
                            return durationBetweenLastTimeActiveAndCurrentTime.compareTo(Duration.of(30, ChronoUnit.SECONDS)) >= 0 && !e.isCriticalLock();
                        })
                        .findFirst();
                if (!entry.isPresent()) {
                    continue;
                }
                entry.get().commit();
                entry.get().shutdown();
                entry.get().setConnection(
                        createConnection(
                                this.property.getConnectionString(),
                                this.property.getUsername(),
                                this.property.getPassword(),
                                this.property.getMaxTimeout()
                        )
                );
                entry.get().deactivate();
                entry.get().activate();
                return entry.get();
            }
            throw new LCXDataSourceException("All connection in pool is busy");
        } catch (Exception e) {
            throw new LCXDataSourceException(e);
        }
    }

    public Connection get() {
        LogUtils.writeLog2(
                LogUtils.Level.INFO,
                String.format(
                        "Pool status:\n    - Total connections:   %d\n    - Active connections:  %d\n    - Idle connections:    %d",
                        this.getTotalConnections(),
                        this.getActiveConnections(),
                        this.getIdleConnections()
                )
        );
        try {
            for (ConnectionEntry connectionEntry : this.pool) {
                if (!connectionEntry.isActive() && !connectionEntry.isCriticalLock()) {
                    connectionEntry.activate();
                    if (connectionEntry.isValid()) {
                        connectionEntry.getConnectionLog().info("Connection is valid");
                        return new LCXConnection(connectionEntry);
                    }
                    connectionEntry.setConnection(
                            createConnection(
                                    this.property.getConnectionString(),
                                    this.property.getUsername(),
                                    this.property.getPassword(),
                                    this.property.getMaxTimeout()
                            )
                    );
                    connectionEntry.getConnectionLog().info("Recreating connection");
                    return new LCXConnection(connectionEntry);
                }
            }
            if (this.pool.size() < this.property.getMaxPoolSize()) {
                val entry = ConnectionEntry.init(
                        createConnection(
                                this.property.getConnectionString(),
                                this.property.getUsername(),
                                this.property.getPassword(),
                                this.property.getMaxTimeout()
                        ),
                        this.dbType,
                        String.format("%s-%s", this.dbType.name().toLowerCase(), generateRandomString(8))
                );
                this.pool.add(entry);
                entry.activate();
                return new LCXConnection(entry);
            }
            LogUtils.writeLog(LogUtils.Level.INFO, "All connections in pool are being used");
            long startTime = System.currentTimeMillis();
            long waitedTime = System.currentTimeMillis() - startTime;
            while (waitedTime < maxPoolWaitingTime) {
                waitedTime = System.currentTimeMillis() - startTime;
                LogUtils.writeLog(LogUtils.Level.INFO, "Waited {} ms", waitedTime);
                val currentTime = DateTimeUtils.generateCurrentTimeDefault();
                val entry = this.pool.stream()
                        .sorted(Comparator.comparing(ConnectionEntry::getLastActiveTime))
                        .filter(e -> {
                            val durationBetweenLastTimeActiveAndCurrentTime = Duration.between(e.getLastActiveTime(), currentTime);
                            return durationBetweenLastTimeActiveAndCurrentTime.compareTo(Duration.of(30, ChronoUnit.SECONDS)) >= 0 && !e.isCriticalLock();
                        })
                        .findFirst();
                if (!entry.isPresent()) {
                    continue;
                }
                entry.get().commit();
                entry.get().shutdown();
                entry.get().setConnection(
                        createConnection(
                                this.property.getConnectionString(),
                                this.property.getUsername(),
                                this.property.getPassword(),
                                this.property.getMaxTimeout()
                        )
                );
                entry.get().deactivate();
                entry.get().activate();
                return new LCXConnection(entry.get());
            }
            throw new LCXDataSourceException("All connection in pool is busy");
        } catch (Exception e) {
            throw new LCXDataSourceException(e);
        }
    }

    public String showDBVersion() {
        var databaseVersion = "0";
        try (
                Connection connection = createConnection(
                        this.property.getConnectionString(),
                        this.property.getUsername(),
                        this.property.getPassword(),
                        this.property.getMaxTimeout()
                );
                PreparedStatement statement = connection.prepareStatement(this.showDbVersionSqlStatement);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                databaseVersion = resultSet.getString(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            LogUtils.writeLog2(e.getMessage(), e);
            throw new LCXDataSourceException(e);
        }
        return databaseVersion;
    }

    public int getTotalConnections() {
        return this.pool.size();
    }

    public int getActiveConnections() {
        return (int) this.pool.stream().filter(con -> !con.getIdle().get()).count();
    }

    public int getIdleConnections() {
        return (int) this.pool.stream().filter(con -> con.getIdle().get()).count();
    }

    public void validateEntry(final ConnectionEntry entry) {
        if (this.pool.stream().anyMatch(con -> con.getConnectionName().equals(entry.getConnectionName()))) {
            try {
                if (entry.isValid()) {
                    return;
                }
                val newConnection = createConnection(
                        this.property.getConnectionString(),
                        this.property.getUsername(),
                        this.property.getPassword(),
                        this.property.getMaxTimeout()
                );
                entry.setConnection(newConnection);
                return;
            } catch (SQLException | ClassNotFoundException e) {
                throw new LCXDataSourceException(e);
            }
        }
        throw new IllegalArgumentException("Invalid connection: " + entry.getConnectionName());
    }

}
