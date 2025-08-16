package com.example.config;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.tool.schema.Action;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.custom.EmptyRoutingContext;

import java.util.HashMap;
import java.util.Map;

import static vn.com.lcx.common.constant.CommonConstant.applicationConfig;

@Component
public class ReactiveHibernateConfiguration {

    @SuppressWarnings({"resource", "CommentedOutCode"})
    @Instance
    public Stage.SessionFactory sessionFactory() {
        final String host = applicationConfig.getPropertyWithEnvironment("server.hreactive.database.host");
        int port;
        try {
            port = Integer.parseInt(applicationConfig.getPropertyWithEnvironment("server.hreactive.database.port"));
        } catch (NumberFormatException e) {
            port = 0;
        }
        final String username = applicationConfig.getPropertyWithEnvironment("server.hreactive.database.username");
        final String password = applicationConfig.getPropertyWithEnvironment("server.hreactive.database.password");
        final String name = applicationConfig.getPropertyWithEnvironment("server.hreactive.database.name");
        DBTypeEnum type;
        try {
            type = DBTypeEnum.valueOf(applicationConfig.getPropertyWithEnvironment("server.hreactive.database.type"));
        } catch (IllegalArgumentException e) {
            type = null;
        }
        int maxPoolSize;
        try {
            maxPoolSize = Integer.parseInt(applicationConfig.getPropertyWithEnvironment("server.hreactive.database.max_pool_size"));
        } catch (NumberFormatException e) {
            maxPoolSize = 0;
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
        final String connectionString = String.format(type.getTemplateUrlConnectionString(), host, port, name);
        Map<String, Object> settings = new HashMap<>();
        settings.put(AvailableSettings.JAKARTA_PERSISTENCE_PROVIDER, "org.hibernate.reactive.provider.ReactivePersistenceProvider");
        settings.put(AvailableSettings.JAKARTA_JDBC_URL, connectionString);
        settings.put(AvailableSettings.JAKARTA_JDBC_USER, username);
        settings.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, password);
        settings.put(AvailableSettings.POOL_SIZE, maxPoolSize);
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, Action.NONE);
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_ACTION, Action.ACTION_UPDATE);
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCRIPT_SOURCE, "data/sql-exported.sql");
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_CREATE_TARGET, "data/sql-exported.sql");
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_DROP_TARGET, "data/sql-exported.sql");
        settings.put(AvailableSettings.FORMAT_SQL, true);
        settings.put(AvailableSettings.HBM2DDL_AUTO, Action.ACTION_NONE);
        ReactiveServiceRegistryBuilder registryBuilder = new ReactiveServiceRegistryBuilder();
        registryBuilder.applySettings(settings);
        MetadataSources metadataSources = new MetadataSources(registryBuilder.build());
        for (Class<?> entity : ClassPool.ENTITIES) {
            metadataSources.addAnnotatedClass(entity);
        }
        Metadata metadata = metadataSources.buildMetadata();

        // EntityManagerFactory emf = Persistence.createEntityManagerFactory("postgresql-example");
        // Stage.SessionFactory factory = emf.unwrap(Stage.SessionFactory.class);
        Stage.SessionFactory factory = metadata.getSessionFactoryBuilder().build().unwrap(Stage.SessionFactory.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Shutting down SessionFactory");
            factory.close();
            // LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Shutting down EntityManagerFactory");
            // emf.close();
        }));
        return factory;
    }

}
