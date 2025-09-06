package vn.com.lcx.vertx.base.custom;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.WorkerExecutor;
import io.vertx.micrometer.MicrometerMetricsFactory;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.annotation.Verticle;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.config.LogbackConfig;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.CommonUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.annotation.app.ComponentScan;
import vn.com.lcx.vertx.base.annotation.app.VertxApplication;
import vn.com.lcx.vertx.base.verticle.VertxBaseVerticle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class MyVertxDeployment {

    private final static MyVertxDeployment INSTANCE = new MyVertxDeployment();

    private MyVertxDeployment() {
    }

    public static MyVertxDeployment getInstance() {
        return INSTANCE;
    }

    private static String getCharacterEncoding() {
        // Creating an array of byte type chars and
        // passing random  alphabet as an argument.abstract
        // Say alphabet be 'w'
        byte[] byteArray = {'w'};

        // Creating an object of InputStream
        InputStream inputStream = new ByteArrayInputStream(byteArray);

        // Now, opening new file input stream reader
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        // Returning default character encoding
        return inputStreamReader.getEncoding();
    }

    private static void logAppStartingStatus(double appStartingTime) {
        final var appFinishingStartingTime = (double) System.currentTimeMillis();
        final var appStartingDuration = (appFinishingStartingTime - appStartingTime) / 1000D;
        LoggerFactory.getLogger("APP").info("Application started in {} second(s)", appStartingDuration);
        // noinspection SystemGetProperty
        LoggerFactory.getLogger("ENCODING").info(
                "Using Java {} - {}\n" +
                        "Encoding information:\n" +
                        "    - Default Charset: {}\n" +
                        "    - Default Charset encoding by java.nio.charset: {}\n" +
                        "    - Default Charset by InputStreamReader: {}",
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                System.getProperty("file.encoding"),
                Charset.defaultCharset().name(),
                getCharacterEncoding()
        );
    }

    private void deployVerticle(final List<String> packagesToScan, Supplier<Void> preconfigure) {
        try (InputStream input = MyVertxDeployment.class.getClassLoader().getResourceAsStream("logback.xml")) {
            if (input == null && System.getProperty("logback.configurationFile") == null) {
                LogbackConfig.configure();
                LogUtils.writeLog(LogUtils.Level.INFO, "Using default logback configuration");
            }
        } catch (Exception ignore) {
        }
        final var oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("vert.x-deployment");
        final var appStartingTime = (double) System.currentTimeMillis();
        try {
            ClassPool.loadProperties();
            boolean enableMetric = Boolean.parseBoolean(
                    CommonConstant.applicationConfig.getPropertyWithEnvironment("server.enable-metrics") + CommonConstant.EMPTY_STRING
            );
            final Vertx vertx;
            if (enableMetric) {
                PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
                registry.config().meterFilter(
                        new MeterFilter() {
                            @SuppressWarnings("NullableProblems")
                            @Override
                            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                                return DistributionStatisticConfig.builder()
                                        .percentilesHistogram(true)
                                        .build()
                                        .merge(config);
                            }
                        });

                vertx = Vertx.builder()
                        .with(new VertxOptions().setMetricsOptions(new MicrometerMetricsOptions()
                                .setEnabled(true)
                                .setPrometheusOptions(new VertxPrometheusOptions()
                                        .setEnabled(true))
                        ))
                        .withMetrics(new MicrometerMetricsFactory(registry))
                        .build();
            } else {
                vertx = Vertx.vertx();
            }
            final WorkerExecutor workerExecutor = vertx.createSharedWorkerExecutor(
                    "lcx-vert.x-worker-pool",
                    20,
                    5,
                    TimeUnit.MINUTES
            );
            ClassPool.setInstance(WorkerExecutor.class.getName(), workerExecutor);
            ClassPool.setInstance("vertx", vertx);
            ClassPool.setInstance(Vertx.class.getName(), vertx);
            if (preconfigure != null) {
                preconfigure.get();
            }
            boolean printUserBanner = false;
            try (InputStream input = MyVertxDeployment.class.getClassLoader().getResourceAsStream("banner.txt")) {
                if (input != null) {
                    printUserBanner = true;
                }
            }
            if (printUserBanner) {
                CommonUtils.bannerLogging("banner.txt");
            } else {
                CommonUtils.bannerLogging("default-banner.txt");
            }
            List<Class<?>> verticles = new ArrayList<>();
            ClassPool.init(packagesToScan, verticles);
            Future<String> deploymentChain = null;
            if (!verticles.isEmpty()) {
                for (Class<?> aClass : verticles) {
                    if (aClass.getAnnotation(Verticle.class) != null) {
                        final VertxBaseVerticle verticle = (VertxBaseVerticle) ClassPool.getInstance(aClass);
                        if (deploymentChain == null) {
                            deploymentChain = vertx.deployVerticle(verticle)
                                    .onFailure(throwable -> LoggerFactory.getLogger("APP").error("Cannot start verticle " + aClass, throwable))
                                    .onSuccess(s -> logVerticleDeploymentId(aClass, s));
                        } else {
                            deploymentChain = deploymentChain.compose(
                                    s -> vertx.deployVerticle(verticle)
                                            .onFailure(throwable -> LoggerFactory.getLogger("APP").error("Cannot start verticle " + aClass, throwable))
                                            .onSuccess(s2 -> logVerticleDeploymentId(aClass, s2))
                            );
                        }
                    }
                }
            }
            if (deploymentChain == null) {
                logAppStartingStatus(appStartingTime);
            } else {
                deploymentChain.onComplete(ar -> {
                    if (ar.succeeded()) {
                        logAppStartingStatus(appStartingTime);
                    } else {
                        LoggerFactory.getLogger("APP").error(
                                ar.cause().getMessage(),
                                ar.cause()
                        );
                        System.exit(1);
                    }
                });
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(ClassPool.class).error(e.getMessage(), e);
            System.exit(1);
        } finally {
            Thread.currentThread().setName(oldThreadName);
        }
    }

    private void logVerticleDeploymentId(Class<?> verticleClass, String verticleDeploymentId) {
        LoggerFactory.getLogger("APP").info("Verticle {} wih deployment ID {} started", verticleClass, verticleDeploymentId);
    }

    public void deployVerticle(Class<?> mainClass, Supplier<Void> preconfigure) {
        if (mainClass.getAnnotation(VertxApplication.class) == null) {
            throw new RuntimeException("Class must by annotated with @VertxApplication");
        }
        final var listOfPackageToScan = new ArrayList<String>();
        listOfPackageToScan.add(mainClass.getPackage().getName());
        if (mainClass.getAnnotation(ComponentScan.class) != null && mainClass.getAnnotation(ComponentScan.class).value().length > 0) {
            final var pkgs = mainClass.getAnnotation(ComponentScan.class).value();
            listOfPackageToScan.addAll(new ArrayList<>(Arrays.asList(pkgs)));
        }
        this.deployVerticle(listOfPackageToScan, preconfigure);
    }

    public void deployVerticle(Class<?> mainClass) {
        deployVerticle(mainClass, null);
    }

}
