package vn.io.lcx.reactive.config;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.mssqlclient.MSSQLBuilder;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.oracleclient.OracleBuilder;
import io.vertx.oracleclient.OracleConnectOptions;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import vn.io.lcx.common.annotation.Component;
import vn.io.lcx.common.annotation.Instance;
import vn.io.lcx.common.constant.CommonConstant;
import vn.io.lcx.common.database.type.DBTypeEnum;
import vn.io.lcx.common.utils.LogUtils;
import vn.io.lcx.vertx.base.custom.EmptyRoutingContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static vn.io.lcx.common.constant.CommonConstant.applicationConfig;

@Component
public class ReactiveDbClientConfiguration {

    private final Vertx vertx;

    public ReactiveDbClientConfiguration(Vertx vertx) {
        this.vertx = vertx;
    }

    public Pool createPg(final int port,
                         final String host,
                         final String database,
                         final String user,
                         final String password,
                         final int maxPoolSize,
                         DBTypeEnum type) {
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(database)
                .setUser(user)
                .setPassword(password);

        PoolOptions poolOptions = new PoolOptions()
                .setIdleTimeout(30)
                .setIdleTimeoutUnit(TimeUnit.SECONDS)
                .setMaxSize(maxPoolSize);

        Pool pool = PgBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final var threadName = "vertx-pg-sql-client-shutdown-hook";
            Thread.currentThread().setName(threadName);
            LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Trying to close pool!");
            final var f = pool.close()
                    .onSuccess(it -> {
                        Thread.currentThread().setName(threadName);
                        LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Released SQL client connection pool");
                    })
                    .onFailure(err -> LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), err.getMessage(), err));
            try {
                f.toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), "Failed to close pool within timeout", e);
            }
        }));
        return getDatabaseVersion(type, pool, "SELECT 1");
    }

    public Pool createMySql(final int port,
                            final String host,
                            final String database,
                            final String user,
                            final String password,
                            final int maxPoolSize,
                            DBTypeEnum type) {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(database)
                .setUser(user)
                .setPassword(password);

        PoolOptions poolOptions = new PoolOptions()
                .setIdleTimeout(30)
                .setIdleTimeoutUnit(TimeUnit.SECONDS)
                .setMaxSize(maxPoolSize);

        Pool pool = MySQLBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final var threadName = "vertx-mysql-sql-client-shutdown-hook";
            Thread.currentThread().setName(threadName);
            LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Trying to close pool!");
            final var f = pool.close()
                    .onSuccess(it -> {
                        Thread.currentThread().setName(threadName);
                        LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Released SQL client connection pool");
                    })
                    .onFailure(err -> LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), err.getMessage(), err));
            try {
                f.toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), "Failed to close pool within timeout", e);
            }
        }));
        return getDatabaseVersion(type, pool, "SELECT 1");
    }

    public Pool createMssql(final int port,
                            final String host,
                            final String database,
                            final String user,
                            final String password,
                            final int maxPoolSize,
                            DBTypeEnum type) {
        MSSQLConnectOptions connectOptions = new MSSQLConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(database)
                .setUser(user)
                .setPassword(password);

        PoolOptions poolOptions = new PoolOptions()
                .setIdleTimeout(30)
                .setIdleTimeoutUnit(TimeUnit.SECONDS)
                .setMaxSize(maxPoolSize);

        Pool pool = MSSQLBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final var threadName = "vertx-mssql-sql-client-shutdown-hook";
            Thread.currentThread().setName(threadName);
            LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Trying to close pool!");
            final var f = pool.close()
                    .onSuccess(it -> {
                        Thread.currentThread().setName(threadName);
                        LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Released SQL client connection pool");
                    })
                    .onFailure(err -> LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), err.getMessage(), err));
            try {
                f.toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), "Failed to close pool within timeout", e);
            }
        }));
        return getDatabaseVersion(type, pool, "SELECT 1");
    }

    public Pool createOracle(final int port,
                             final String host,
                             final String database,
                             final String user,
                             final String password,
                             final int maxPoolSize,
                             DBTypeEnum type) {
        OracleConnectOptions connectOptions = new OracleConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(database)
                .setUser(user)
                .setPassword(password);

        PoolOptions poolOptions = new PoolOptions()
                .setIdleTimeout(30)
                .setIdleTimeoutUnit(TimeUnit.SECONDS)
                .setMaxSize(maxPoolSize);

        Pool pool = OracleBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final var threadName = "vertx-oracle-sql-client-shutdown-hook";
            Thread.currentThread().setName(threadName);
            LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Trying to close pool!");
            final var f = pool.close()
                    .onSuccess(it -> {
                        Thread.currentThread().setName(threadName);
                        LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Released SQL client connection pool");
                    })
                    .onFailure(err -> LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), err.getMessage(), err));
            try {
                f.toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), "Failed to close pool within timeout", e);
            }
        }));
        return getDatabaseVersion(type, pool, "SELECT 1 FROM dual");
    }

    public Pool createJdbc(final int port,
                           final String host,
                           final String database,
                           final String user,
                           final String password,
                           final int maxPoolSize,
                           DBTypeEnum type) {
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
                .setJdbcUrl(
                        String.format(type.getTemplateUrlConnectionString(), host, port, database)
                )
                .setUser(user)
                .setPassword(password);

        PoolOptions poolOptions = new PoolOptions()
                .setIdleTimeout(30)
                .setIdleTimeoutUnit(TimeUnit.SECONDS)
                .setMaxSize(maxPoolSize);

        Pool pool = JDBCPool.pool(vertx, connectOptions, poolOptions);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final var threadName = "vertx-mssql-sql-client-shutdown-hook";
            Thread.currentThread().setName(threadName);
            LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Trying to close pool!");
            final var f = pool.close()
                    .onSuccess(it -> {
                        Thread.currentThread().setName(threadName);
                        LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, "Released SQL client connection pool");
                    })
                    .onFailure(err -> LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), err.getMessage(), err));
            try {
                f.toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), "Failed to close pool within timeout", e);
            }
        }));
        return getDatabaseVersion(type, pool, "SELECT 1");
    }

    private Pool getDatabaseVersion(DBTypeEnum type, Pool pool, String verifyStatement) {
        final var future = pool.withConnection(conn -> showDbVersion(conn, type))
                .onSuccess(
                        result -> {
                            LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), LogUtils.Level.INFO, result);
                            refreshConnection(
                                    () ->
                                            pool.withConnection(connection ->
                                                    connection.query(verifyStatement)
                                                            .execute()
                                                            .map(rows -> CommonConstant.VOID)
                                            )
                            );
                        }
                )
                .onFailure(
                        err -> {
                            LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(), "An error occurred in initialization stage of reactive database pool", err);
                        }
                );
        try {
            future.toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new ExceptionInInitializerError("Database connection timed out after 30 seconds");
        } catch (ExecutionException e) {
            throw new ExceptionInInitializerError("Cannot create reactive database pool");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExceptionInInitializerError("Database connection was interrupted");
        }
        return pool;
    }

    private Future<String> showDbVersion(SqlConnection conn, DBTypeEnum type) {
        return conn.query(type.getShowDbVersionSqlStatement())
                .execute()
                .map(rowSet -> {
                    final StringBuilder info = new StringBuilder(conn.databaseMetadata().productName()).append(": ");
                    for (Row row : rowSet) {
                        info.append(row.getString(0));
                    }
                    return info.toString();
                });
    }

    @Instance
    public Pool pool() {
        String host = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.database.host");
        int port;
        try {
            port = Integer.parseInt(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.database.port"));
        } catch (NumberFormatException e) {
            port = 0;
        }
        String username = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.database.username");
        String password = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.database.password");
        String name = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.database.name");
        int maxPoolSize;
        try {
            maxPoolSize = Integer.parseInt(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.database.max_pool_size"));
        } catch (NumberFormatException e) {
            maxPoolSize = 0;
        }
        DBTypeEnum type;
        try {
            type = DBTypeEnum.valueOf(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.reactive.database.type"));
        } catch (IllegalArgumentException e) {
            type = null;
        }
        if (
                host.equals(CommonConstant.NULL_STRING) ||
                        username.equals(CommonConstant.NULL_STRING) ||
                        password.equals(CommonConstant.NULL_STRING) ||
                        name.equals(CommonConstant.NULL_STRING) ||
                        port == 0 ||
                        maxPoolSize == 0 ||
                        type == null
        ) {
            return null;
        }
        return switch (type) {
            case POSTGRESQL -> createPg(port, host, name, username, password, maxPoolSize, type);
            case MYSQL -> createMySql(port, host, name, username, password, maxPoolSize, type);
            case MSSQL -> createMssql(port, host, name, username, password, maxPoolSize, type);
            case ORACLE -> createOracle(port, host, name, username, password, maxPoolSize, type);
        };
    }

    public void refreshConnection(Supplier<Future<Void>> handler) {
        long period = 30_000; // 30 seconds
        long now = System.currentTimeMillis();
        long next = ((now / period) + 1) * period; // next 30 seconds
        long initialDelay = next - now;
        vertx.setTimer(initialDelay, id ->
                handler.get()
                        .onSuccess(v ->
                                vertx.setPeriodic(period, pid -> {
                                    handler.get()
                                            .onFailure(e ->
                                                    LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(),
                                                            "Connection invalid",
                                                            e)
                                            );
                                })
                        )
                        .onFailure(e ->
                                LogUtils.writeLog("Reactive-Pool-Configuration", EmptyRoutingContext.init(),
                                        "Connection invalid",
                                        e)
                        )
        );
    }

}
