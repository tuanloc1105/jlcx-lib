package vn.com.lcx.reactive.config;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
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
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.custom.EmptyRoutingContext;

import static vn.com.lcx.common.constant.CommonConstant.applicationConfig;

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
                .setMaxSize(maxPoolSize);

        Pool pool = PgBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> pool.close()
                .onSuccess(it -> {
                    Thread.currentThread().setName("vertx-pg-sql-client-shutdown-hook");
                    LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Released SQL client connection pool");
                })
                .onFailure(err -> LogUtils.writeLog(EmptyRoutingContext.init(), err.getMessage(), err))));
        return getDatabaseVersion(type, pool);
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
                .setMaxSize(maxPoolSize);

        Pool pool = MySQLBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> pool.close()
                .onSuccess(it -> {
                    Thread.currentThread().setName("vertx-mysql-sql-client-shutdown-hook");
                    LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Released SQL client connection pool");
                })
                .onFailure(err -> LogUtils.writeLog(EmptyRoutingContext.init(), err.getMessage(), err))));
        return getDatabaseVersion(type, pool);
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
                .setMaxSize(maxPoolSize);

        Pool pool = MSSQLBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> pool.close()
                .onSuccess(it -> {
                    Thread.currentThread().setName("vertx-mssql-sql-client-shutdown-hook");
                    LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Released SQL client connection pool");
                })
                .onFailure(err -> LogUtils.writeLog(EmptyRoutingContext.init(), err.getMessage(), err))));
        return getDatabaseVersion(type, pool);
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
                .setMaxSize(maxPoolSize);

        Pool pool = OracleBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> pool.close()
                .onSuccess(it -> {
                    Thread.currentThread().setName("vertx-oracle-sql-client-shutdown-hook");
                    LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Released SQL client connection pool");
                })
                .onFailure(err -> LogUtils.writeLog(EmptyRoutingContext.init(), err.getMessage(), err))));
        return getDatabaseVersion(type, pool);
    }

    private Pool getDatabaseVersion(DBTypeEnum type, Pool pool) {
        final var future = pool.withConnection(conn ->
                        showDbVersion(conn, type)
                                .eventually(conn::close)
                )
                .onSuccess(
                        result -> LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, result)
                )
                .onFailure(
                        err -> {
                            LogUtils.writeLog(EmptyRoutingContext.init(), err.getMessage(), err);
                            System.exit(1);
                        }
                );
        // noinspection StatementWithEmptyBody
        while (!future.isComplete()) {
            // wait
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
        Pool pool = null;
        switch (type) {
            case POSTGRESQL:
                pool = createPg(port, host, name, username, password, maxPoolSize, type);
                break;
            case MYSQL:
                pool = createMySql(port, host, name, username, password, maxPoolSize, type);
                break;
            case MSSQL:
                pool = createMssql(port, host, name, username, password, maxPoolSize, type);
                break;
            case ORACLE:
                pool = createOracle(port, host, name, username, password, maxPoolSize, type);
                break;
        }
        return pool;
    }

}
