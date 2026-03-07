package vn.io.lcx.reactive.config;

import io.vertx.core.Vertx;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.vertx.VertxInstance;
import org.hibernate.tool.schema.Action;
import org.hibernate.tool.schema.SourceType;
import vn.io.lcx.common.annotation.Component;
import vn.io.lcx.common.annotation.Instance;
import vn.io.lcx.common.config.ClassPool;
import vn.io.lcx.common.constant.CommonConstant;
import vn.io.lcx.common.database.type.DBTypeEnum;
import vn.io.lcx.common.utils.LogUtils;
import vn.io.lcx.vertx.base.custom.EmptyRoutingContext;

import java.util.HashMap;
import java.util.Map;

import static vn.io.lcx.common.constant.CommonConstant.applicationConfig;

@Component
public class ReactiveHibernateConfiguration {

    private final Vertx vertx;

    public ReactiveHibernateConfiguration(Vertx vertx) {
        this.vertx = vertx;
    }

    public static Stage.SessionFactory createHreactiveSessionFactory(Vertx vertx, DBTypeEnum type, String host, int port, String name, String username, String password, int maxPoolSize) {
        final String connectionString = String.format(type.getTemplateUrlConnectionString(), host, port, name);
        Map<String, Object> settings = new HashMap<>();
        settings.put(AvailableSettings.JAKARTA_PERSISTENCE_PROVIDER, "org.hibernate.reactive.provider.ReactivePersistenceProvider");
        settings.put(AvailableSettings.JAKARTA_JDBC_URL, connectionString);
        settings.put(AvailableSettings.JAKARTA_JDBC_USER, username);
        settings.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, password);
        settings.put(AvailableSettings.POOL_SIZE, maxPoolSize);
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, Action.NONE);
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_ACTION, Action.ACTION_UPDATE);
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SOURCE, SourceType.METADATA);
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_CREATE_SCRIPT_SOURCE, "data/sql-script-source-exported.sql");
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_CREATE_TARGET, "data/sql-script-create-target-exported.sql");
        settings.put(AvailableSettings.JAKARTA_HBM2DDL_SCRIPTS_DROP_TARGET, "data/sql-script-drop-target-exported.sql");
        settings.put(AvailableSettings.FORMAT_SQL, true);
        settings.put(AvailableSettings.HBM2DDL_AUTO, Action.ACTION_NONE);
        ReactiveServiceRegistryBuilder registryBuilder = new ReactiveServiceRegistryBuilder();
        registryBuilder.applySettings(settings);
        registryBuilder.addService(VertxInstance.class, (VertxInstance) () -> vertx);
        MetadataSources metadataSources = new MetadataSources(registryBuilder.build());
        for (Class<?> entity : ClassPool.getEntities()) {
            metadataSources.addAnnotatedClass(entity);
        }
        Metadata metadata = metadataSources.buildMetadata();

        // EntityManagerFactory emf = Persistence.createEntityManagerFactory("postgresql-example");
        // Stage.SessionFactory factory = emf.unwrap(Stage.SessionFactory.class);
        @SuppressWarnings("resource") Stage.SessionFactory factory = metadata.getSessionFactoryBuilder().build().unwrap(Stage.SessionFactory.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("hreactive-shutdown-hook");
            LogUtils.writeLog(ReactiveHibernateConfiguration.class, EmptyRoutingContext.init(), LogUtils.Level.INFO, "Shutting down SessionFactory");
            factory.close();
            // LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Shutting down EntityManagerFactory");
            // emf.close();
        }));
        return factory;
    }

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
        return createHreactiveSessionFactory(vertx, type, host, port, name, username, password, maxPoolSize);
    }

}
