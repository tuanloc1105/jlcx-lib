package vn.com.lcx.jpa.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.JdbcSettings;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.Action;
import org.hibernate.tool.schema.TargetType;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.PostConstruct;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.scanner.PackageScanner;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.jpa.context.EntityContainer;

import java.util.ArrayList;
import java.util.EnumSet;
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
        final var sessionFactory = createSessionFactory(
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
                type,
                null,
                true,
                true
        );
        ClassPool.setInstance(sessionFactory);
        ClassPool.setInstance(SessionFactory.class.getName(), sessionFactory);
    }

    public static SessionFactory createSessionFactory(final String host,
                                                      final int port,
                                                      final String username,
                                                      final String password,
                                                      final String name,
                                                      final String driverClassName,
                                                      final String dialectName,
                                                      final int initialPoolSize,
                                                      final int maxPoolSize,
                                                      final int maxTimeout,
                                                      final DBTypeEnum dbType,
                                                      final String entityPackage,
                                                      final boolean doSchemaExport,
                                                      final boolean doSchemaUpdate) {
        StandardServiceRegistry registry = null;
        SessionFactory sessionFactory;
        HikariDataSource dataSource;
        final var entitiesClassNames = new ArrayList<String>();
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

            dataSource = new HikariDataSource(hikariConfig);
            settings.put(JdbcSettings.JAKARTA_JTA_DATASOURCE, dataSource);
            // settings.put(
            //         Environment.DIALECT,
            //         StringUtils.isBlank(dialectName) ||
            //                 dialectName.equals(CommonConstant.NULL_STRING) ? dbType.getDialectClass() : dialectName
            // );
            // settings.put(Environment.SHOW_SQL, true);
            settings.put(Environment.FORMAT_SQL, true);
            // settings.put(JdbcSettings.HIGHLIGHT_SQL, true);
            settings.put(JdbcSettings.DIALECT_NATIVE_PARAM_MARKERS, true);
            settings.put(Environment.HBM2DDL_AUTO, Action.ACTION_NONE);

            settings.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, PhysicalNamingStrategyStandardImpl.class.getName());
            settings.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_ACTION, Action.ACTION_NONE);

            // final var generatedDdlFilePath = FileUtils.pathJoining(
            //         CommonConstant.ROOT_DIRECTORY_PROJECT_PATH,
            //         "data",
            //         name.toLowerCase() + "-generated-ddl.sql"
            // );
            // FileUtils.delete(generatedDdlFilePath);
            // FileUtils.createFile(generatedDdlFilePath);

            // settings.put(
            //         SchemaToolingSettings.JAKARTA_HBM2DDL_SCRIPTS_CREATE_TARGET,
            //         generatedDdlFilePath
            // );

            // settings.put(
            //         SchemaToolingSettings.JAKARTA_HBM2DDL_SCRIPTS_DROP_TARGET,
            //         generatedDdlFilePath
            // );

            registryBuilder.applySettings(settings);

            registry = registryBuilder.build();
            MetadataSources sources = new MetadataSources(registry);
            if (StringUtils.isBlank(entityPackage)) {
                for (Class<?> entity : ClassPool.ENTITIES) {
                    sources.addAnnotatedClass(entity);
                    entitiesClassNames.add(entity.getName());
                }
            } else {
                final var entitiesInPackage = PackageScanner.findClasses(entityPackage);
                for (Class<?> entity : entitiesInPackage) {
                    sources.addAnnotatedClass(entity);
                    entitiesClassNames.add(entity.getName());
                }
            }
            Metadata metadata = sources.getMetadataBuilder().build();

            try {
                if (doSchemaExport) {
                    final var schemaExportDdlFilePath = FileUtils.pathJoining(
                            CommonConstant.ROOT_DIRECTORY_PROJECT_PATH,
                            "data",
                            name.toLowerCase() + "-schema-export-ddl.sql"
                    );
                    FileUtils.delete(schemaExportDdlFilePath);
                    FileUtils.createFile(schemaExportDdlFilePath);
                    SchemaExport schemaExport = new SchemaExport();
                    schemaExport.setOutputFile(schemaExportDdlFilePath);
                    schemaExport.setFormat(true);
                    schemaExport.setDelimiter(";");
                    schemaExport.create(EnumSet.of(TargetType.SCRIPT), metadata);
                }
                if (doSchemaUpdate) {
                    final var schemaUpdateDdlFilePath = FileUtils.pathJoining(
                            CommonConstant.ROOT_DIRECTORY_PROJECT_PATH,
                            "data",
                            name.toLowerCase() + "-schema-update-ddl.sql"
                    );
                    FileUtils.delete(schemaUpdateDdlFilePath);
                    FileUtils.createFile(schemaUpdateDdlFilePath);
                    SchemaUpdate schemaUpdate = new SchemaUpdate();
                    schemaUpdate.setOutputFile(schemaUpdateDdlFilePath);
                    schemaUpdate.setFormat(true);
                    schemaUpdate.setDelimiter(";");
                    schemaUpdate.execute(EnumSet.of(TargetType.SCRIPT), metadata);
                }
            } catch (Exception ddlException) {
                LogUtils.writeLog("An error occurred while exporting DDL script: " + ddlException.getMessage(), ddlException);
            }

            sessionFactory = metadata.getSessionFactoryBuilder().build();
        } catch (Exception e) {
            if (registry != null) {
                StandardServiceRegistryBuilder.destroy(registry);
            }
            LogUtils.writeLog(e.getMessage(), e);
            throw new ExceptionInInitializerError("Failed to initialize Hibernate SessionFactory");
        }
        final StandardServiceRegistry finalRegistry = registry;
        final SessionFactory finalSessionFactory = sessionFactory;
        final HikariDataSource finalDataSource = dataSource;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalSessionFactory != null && !finalSessionFactory.isClosed()) {
                finalSessionFactory.close();
            }
            StandardServiceRegistryBuilder.destroy(finalRegistry);
            finalDataSource.close();
        }));
        EntityContainer.addEntityManager(entitiesClassNames, finalSessionFactory);
        return sessionFactory;
    }

}
