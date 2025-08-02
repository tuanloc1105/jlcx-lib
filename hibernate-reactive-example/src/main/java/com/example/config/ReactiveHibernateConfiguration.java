package com.example.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.reactive.stage.Stage;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.custom.EmptyRoutingContext;

@Component
public class ReactiveHibernateConfiguration {

    @SuppressWarnings("resource")
    @Instance
    public Stage.SessionFactory sessionFactory() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("postgresql-example");
        Stage.SessionFactory factory = emf.unwrap(Stage.SessionFactory.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Shutting down SessionFactory");
            factory.close();
            LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Shutting down EntityManagerFactory");
            emf.close();
        }));
        return factory;
    }

}
