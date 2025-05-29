package vn.com.lcx.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.JdbcSettings;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.PostConstruct;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

import static vn.com.lcx.common.constant.CommonConstant.applicationConfig;

@Component
public class HibernateConfiguration {

    @PostConstruct
    public void getSessionFactory() {
        String host = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.host");
        int port;
        try {
            port = Integer.parseInt(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.port"));
        } catch (NumberFormatException e) {
            port = 0;
        }
        String username = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.username");
        String password = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.password");
        String name = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.name");
        String driverClassName = applicationConfig.getPropertyWithEnvironment("server.database.driver_class_name");
        String dialectName = applicationConfig.getPropertyWithEnvironment("server.database.dialect");
        int initialPoolSize;
        try {
            initialPoolSize = Integer.parseInt(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.initial_pool_size"));
        } catch (NumberFormatException e) {
            initialPoolSize = 0;
        }
        int maxPoolSize;
        try {
            maxPoolSize = Integer.parseInt(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.max_pool_size"));
        } catch (NumberFormatException e) {
            maxPoolSize = 0;
        }
        int maxTimeout;
        try {
            maxTimeout = Integer.parseInt(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.max_timeout"));
        } catch (NumberFormatException e) {
            maxTimeout = 0;
        }
        DBTypeEnum type;
        try {
            type = DBTypeEnum.valueOf(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.type"));
        } catch (IllegalArgumentException e) {
            type = null;
        }
        if (
                host.equals(CommonConstant.NULL_STRING) ||
                        username.equals(CommonConstant.NULL_STRING) ||
                        password.equals(CommonConstant.NULL_STRING) ||
                        name.equals(CommonConstant.NULL_STRING) ||
                        // driverClassName.equals(CommonConstant.NULL_STRING) ||
                        port == 0 ||
                        initialPoolSize == 0 ||
                        maxPoolSize == 0 ||
                        maxTimeout == 0 ||
                        type == null
        ) {
            return;
        }
        createSessionFactory(
                host,
                port,
                username,
                password,
                name,
                driverClassName,
                dialectName,
                initialPoolSize,
                maxPoolSize,
                maxTimeout,
                type
        );
    }

    public SessionFactory createSessionFactory(String host,
                                               int port,
                                               String username,
                                               String password,
                                               String name,
                                               String driverClassName,
                                               String dialectName,
                                               int initialPoolSize,
                                               int maxPoolSize,
                                               int maxTimeout,
                                               DBTypeEnum dbType) {
        StandardServiceRegistry registry = null;
        SessionFactory sessionFactory = null;
        try {
            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
            Map<String, Object> settings = new HashMap<>();
            final String connectionString = String.format(dbType.getTemplateUrlConnectionString(), host, port, name);
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(connectionString);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setDriverClassName(
                    StringUtils.isBlank(driverClassName) ||
                            driverClassName.equals(CommonConstant.NULL_STRING) ? dbType.getDefaultDriverClassName() : driverClassName
            );
            hikariConfig.setMaximumPoolSize(maxPoolSize);
            hikariConfig.setMinimumIdle(initialPoolSize);
            hikariConfig.setIdleTimeout(300000);
            hikariConfig.setConnectionTimeout(maxTimeout * 1000L);
            hikariConfig.setMaxLifetime(1800000);
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            settings.put(JdbcSettings.JAKARTA_JTA_DATASOURCE, "dataSource");
            settings.put(
                    Environment.DIALECT,
                    StringUtils.isBlank(dialectName) ||
                            dialectName.equals(CommonConstant.NULL_STRING) ? dbType.getDialectClass() : dialectName
            );
            settings.put(Environment.SHOW_SQL, "true");
            settings.put(Environment.FORMAT_SQL, "true");
            settings.put(Environment.HBM2DDL_AUTO, "none"); // validate | update | create | create-drop | none

            registryBuilder.applySettings(settings);
            registryBuilder.applySetting(JdbcSettings.JAKARTA_JTA_DATASOURCE, dataSource);

            registry = registryBuilder.build();
            MetadataSources sources = new MetadataSources(registry);
            // sources.addAnnotatedClass(AnotherEntity.class);
            Metadata metadata = sources.getMetadataBuilder().build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();
        } catch (Exception e) {
            if (registry != null) {
                StandardServiceRegistryBuilder.destroy(registry);
            }
            LogUtils.writeLog(e.getMessage(), e);
            throw new ExceptionInInitializerError("Failed to initialize Hibernate SessionFactory.");
        }
        StandardServiceRegistry finalRegistry = registry;
        SessionFactory finalSessionFactory = sessionFactory;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            StandardServiceRegistryBuilder.destroy(finalRegistry);
            if (finalSessionFactory != null && !finalSessionFactory.isClosed()) {
                finalSessionFactory.close();
            }
        }));
        return sessionFactory;
    }

}
