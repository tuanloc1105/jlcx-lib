package vn.com.lcx.vertx.base.custom;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.annotation.Verticle;
import vn.com.lcx.common.config.ClassPool;
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

import static vn.com.lcx.common.config.ClassPool.CLASS_POOL;

@Slf4j
public class MyVertxDeployment {

    private final static MyVertxDeployment INSTANCE = new MyVertxDeployment();

    private final Vertx vertx;

    private MyVertxDeployment() {
        vertx = Vertx.vertx();
        CLASS_POOL.put("vertx", vertx);
        CLASS_POOL.put(VertxBaseVerticle.class.getName(), vertx);
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
        try {
            if (preconfigure != null) {
                preconfigure.get();
            }
            List<Class<?>> verticles = new ArrayList<>();
            val appStartingTime = (double) System.currentTimeMillis();
            ClassPool.init(packagesToScan, verticles);
            if (verticles.isEmpty()) {
                return;
            }
            List<Future<String>> listOfVerticleFuture = new ArrayList<>();
            for (Class<?> aClass : verticles) {
                if (aClass.getAnnotation(Verticle.class) != null) {
                    val fields = Arrays.stream(aClass.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())).collect(Collectors.toList());
                    final Class<?>[] fieldArr = fields.stream().map(Field::getType).toArray(Class[]::new);
                    final Object[] args = fields.stream().map(
                            f -> {
                                Object o1 = CLASS_POOL.get(f.getName());
                                if (o1 != null) {
                                    return o1;
                                }
                                return CLASS_POOL.get(f.getType().getName());
                            }
                    ).toArray(Object[]::new);
                    final VertxBaseVerticle verticle = (VertxBaseVerticle) aClass.getDeclaredConstructor(fieldArr).newInstance(args);
                    final Future<String> applicationVerticleFuture = this.vertx.deployVerticle(verticle);
                    applicationVerticleFuture.onFailure(throwable -> LoggerFactory.getLogger("APP").error("Cannot start verticle {}", aClass, throwable));
                    applicationVerticleFuture.onSuccess(s -> {
                        LoggerFactory.getLogger("APP").info("Verticle {} wih deployment ID {} started", aClass, s);
                    });
                    listOfVerticleFuture.add(applicationVerticleFuture);
                }
            }
            if (!listOfVerticleFuture.isEmpty()) {
                while (listOfVerticleFuture.stream().noneMatch(Future::isComplete)) {
                }
                val appFinishingStartingTime = (double) System.currentTimeMillis();
                val appStartingDuration = (appFinishingStartingTime - appStartingTime) / 1000D;
                LoggerFactory.getLogger("APP").info("Application started in {} second(s)", appStartingDuration);
                //noinspection SystemGetProperty
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
            throw new RuntimeException(e);
        }

    }

    private void deployVerticle(String packageToScan, Supplier<Void> preconfigure) {
        this.deployVerticle(new ArrayList<>(Collections.singleton(packageToScan)), preconfigure);
    }

    public void deployVerticle(Class<?> mainClass, Supplier<Void> preconfigure) {
        if (mainClass.getAnnotation(VertxApplication.class) == null) {
            throw new RuntimeException("Class must by annotated with @VertxApplication");
        }
        val listOfPackageToScan = new ArrayList<String>();
        listOfPackageToScan.add(mainClass.getPackage().getName());
        if (mainClass.getAnnotation(ComponentScan.class) != null && mainClass.getAnnotation(ComponentScan.class).value().length > 0) {
            val pkgs = mainClass.getAnnotation(ComponentScan.class).value();
            listOfPackageToScan.addAll(new ArrayList<>(Arrays.asList(pkgs)));
        }
        this.deployVerticle(listOfPackageToScan, preconfigure);
    }

    public void deployVerticle(Class<?> mainClass) {
        if (mainClass.getAnnotation(VertxApplication.class) == null) {
            throw new RuntimeException("Class must by annotated with @VertxApplication");
        }
        val listOfPackageToScan = new ArrayList<String>();
        listOfPackageToScan.add(mainClass.getPackage().getName());
        if (mainClass.getAnnotation(ComponentScan.class) != null && mainClass.getAnnotation(ComponentScan.class).value().length > 0) {
            val pkgs = mainClass.getAnnotation(ComponentScan.class).value();
            listOfPackageToScan.addAll(new ArrayList<>(Arrays.asList(pkgs)));
        }
        this.deployVerticle(listOfPackageToScan, null);
    }

}
