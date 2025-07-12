package vn.com.lcx.vertx.base.custom;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.micrometer.MicrometerMetricsFactory;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.annotation.Verticle;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.vertx.base.annotation.app.ComponentScan;
import vn.com.lcx.vertx.base.annotation.app.VertxApplication;
import vn.com.lcx.vertx.base.verticle.VertxBaseVerticle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private void deployVerticle(final List<String> packagesToScan, Supplier<Void> preconfigure) {
        final var oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("vertx-deployment");
        try {
            ClassPool.loadProperties();
            boolean enableMetric = Boolean.parseBoolean(
                    CommonConstant.applicationConfig.getPropertyWithEnvironment("server.enable-metrics") + CommonConstant.EMPTY_STRING
            );
            final Vertx vertx;
            if (enableMetric) {
                // PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
                // registry.config().meterFilter(
                //         new MeterFilter() {
                //             @Override
                //             public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                //                 return DistributionStatisticConfig.builder()
                //                         .percentilesHistogram(true)
                //                         .build()
                //                         .merge(config);
                //             }
                //         });

                vertx = Vertx.builder()
                        .with(new VertxOptions().setMetricsOptions(new MicrometerMetricsOptions()
                                .setEnabled(true)
                                .setPrometheusOptions(new VertxPrometheusOptions()
                                        .setEnabled(true))
                        ))
                        // .withMetrics(new MicrometerMetricsFactory(registry))
                        .build();
            } else {
                vertx = Vertx.vertx();
            }
            ClassPool.setInstance("vertx", vertx);
            ClassPool.setInstance(VertxBaseVerticle.class.getName(), vertx);
            if (preconfigure != null) {
                preconfigure.get();
            }
            List<Class<?>> verticles = new ArrayList<>();
            final var appStartingTime = (double) System.currentTimeMillis();
            ClassPool.init(packagesToScan, verticles);
            if (verticles.isEmpty()) {
                return;
            }
            List<Future<String>> listOfVerticleFuture = new ArrayList<>();
            for (Class<?> aClass : verticles) {
                if (aClass.getAnnotation(Verticle.class) != null) {
                    final var fields = Arrays.stream(aClass.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())).collect(Collectors.toList());
                    final Class<?>[] fieldArr = fields.stream().map(Field::getType).toArray(Class[]::new);
                    final Object[] args = fields.stream().map(
                            f -> {
                                Object o1 = ClassPool.getInstance(f.getName());
                                if (o1 != null) {
                                    return o1;
                                }
                                return ClassPool.getInstance(f.getType().getName());
                            }
                    ).toArray(Object[]::new);
                    final VertxBaseVerticle verticle = (VertxBaseVerticle) aClass.getDeclaredConstructor(fieldArr).newInstance(args);
                    final Future<String> applicationVerticleFuture = vertx.deployVerticle(verticle);
                    applicationVerticleFuture.onFailure(throwable -> LoggerFactory.getLogger("APP").error("Cannot start verticle {}", aClass, throwable));
                    applicationVerticleFuture.onSuccess(s -> {
                        LoggerFactory.getLogger("APP").info("Verticle {} wih deployment ID {} started", aClass, s);
                    });
                    listOfVerticleFuture.add(applicationVerticleFuture);
                }
            }
            if (!listOfVerticleFuture.isEmpty()) {
                JsonArray results = new JsonArray();
                // noinspection StatementWithEmptyBody
                while (listOfVerticleFuture.stream().noneMatch(Future::isComplete)) {
                    // do nothing here
                }
                final var appFinishingStartingTime = (double) System.currentTimeMillis();
                final var appStartingDuration = (appFinishingStartingTime - appStartingTime) / 1000D;
                LoggerFactory.getLogger("APP").info("Application started in {} second(s)", appStartingDuration);
                // noinspection SystemGetProperty
                LoggerFactory.getLogger("ENCODING").info(
                        "Using Java {} - {}\nEncoding information:\n    - Default Charset: {}\n    - Default Charset encoding by java.nio.charset: {}\n    - Default Charset by InputStreamReader: {}",
                        System.getProperty("java.version"),
                        System.getProperty("java.vendor"),
                        System.getProperty("file.encoding"),
                        Charset.defaultCharset().name(),
                        getCharacterEncoding()
                );
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(ClassPool.class).error(e.getMessage(), e);
            System.exit(1);
        } finally {
            Thread.currentThread().setName(oldThreadName);
        }
    }

    private void deployVerticle(String packageToScan, Supplier<Void> preconfigure) {
        this.deployVerticle(new ArrayList<>(Collections.singleton(packageToScan)), preconfigure);
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
