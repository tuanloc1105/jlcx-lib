package vn.com.lcx.common.database.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import vn.com.lcx.common.database.DatabaseProperty;
import vn.com.lcx.common.database.pool.wrapper.HikariConnectionWrapper;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.exception.HikariLcxDataSourceException;
import vn.com.lcx.common.exception.LCXDataSourceException;
import vn.com.lcx.common.exception.LCXDataSourcePropertiesException;
import vn.com.lcx.common.utils.LogUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A wrapper class of HikariCP
 * No longer maintained
 */
public class HikariLcxDataSource extends LCXDataSource {

    private final HikariDataSource dataSource;

    public HikariLcxDataSource(
            String poolName,
            String defaultDriverClassName,
            DatabaseProperty property,
            DBTypeEnum dbType,
            HikariDataSource dataSource
    ) {
        super(
                poolName,
                defaultDriverClassName,
                null,
                property,
                null,
                dbType,
                0,
                null
        );
        this.dataSource = dataSource;
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
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(connectionString);
                config.setUsername(username);
                config.setPassword(password);

                config.setMaximumPoolSize(maxPoolSize);
                config.setMinimumIdle(initialPoolSize);
                config.setIdleTimeout(30000);
                config.setConnectionTimeout(30000);
                config.setLeakDetectionThreshold(15000);
                config.setDriverClassName(driverClassName);
                final var hikariDs = new HikariDataSource(config);
                final var pool = new HikariLcxDataSource(
                        databaseName,
                        property.getDriverClassName(),
                        property,
                        dbType,
                        hikariDs
                );
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    Thread.currentThread().setName("shutdown");
                    hikariDs.close();
                }));
                return pool;
            }
            throw new LCXDataSourcePropertiesException("Database properties is not all set");
        } catch (Exception e) {
            LogUtils.writeLog2(e.getMessage(), e);
            throw new LCXDataSourceException(e);
        }
    }

    @Override
    public Connection get() {
        try {
            LogUtils.writeLog2(
                    LogUtils.Level.INFO,
                    String.format(
                            "Pool status:\n    - Total connections:   %d\n    - Active connections:  %d\n    - Idle connections:    %d",
                            dataSource.getHikariPoolMXBean().getTotalConnections(),
                            dataSource.getHikariPoolMXBean().getActiveConnections(),
                            dataSource.getHikariPoolMXBean().getIdleConnections()
                    )
            );
            return new HikariConnectionWrapper(dataSource.getConnection(), getDbType());
        } catch (SQLException e) {
            throw new HikariLcxDataSourceException(e);
        }
    }

    @Override
    public String showDBVersion() {
        var databaseVersion = "0";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(getDbType().getShowDbVersionSqlStatement());
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                databaseVersion = resultSet.getString(1);
            }
        } catch (SQLException e) {
            LogUtils.writeLog2(e.getMessage(), e);
            throw new LCXDataSourceException(e);
        }
        return databaseVersion;
    }

}
