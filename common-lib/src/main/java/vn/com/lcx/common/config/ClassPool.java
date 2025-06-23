package vn.com.lcx.common.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.annotation.InstanceClass;
import vn.com.lcx.common.annotation.PostConstruct;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.annotation.Verticle;
import vn.com.lcx.common.annotation.mapper.Mapper;
import vn.com.lcx.common.annotation.mapper.MapperClass;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.pool.HikariLcxDataSource;
import vn.com.lcx.common.database.pool.LCXDataSource;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.database.utils.EntityUtils;
import vn.com.lcx.common.scanner.PackageScanner;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.ObjectUtils;
import vn.com.lcx.common.utils.PropertiesUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static vn.com.lcx.common.constant.CommonConstant.applicationConfig;
import static vn.com.lcx.common.utils.FileUtils.createFolderIfNotExists;

public class ClassPool {

    public static final List<Class<?>> ENTITIES = new ArrayList<>();
    private static final ConcurrentHashMap<String, Object> CLASS_POOL = new ConcurrentHashMap<>();

    public static void init(final List<String> packagesToScan, final List<Class<?>> verticleClass) {
        packagesToScan.add("vn.com.lcx");
        // loadProperties();
        try {
            final List<Class<?>> listOfClassInPackage = new ArrayList<>();
            packagesToScan.forEach(packageName -> {
                try {
                    listOfClassInPackage.addAll(PackageScanner.findClasses(packageName));
                } catch (Exception ignore) {
                }
            });

            final var setOfClassInPackage = new ArrayList<>(new HashSet<>(listOfClassInPackage));
            listOfClassInPackage.clear();
            listOfClassInPackage.addAll(setOfClassInPackage);

            final var sourceType = CommonConstant.applicationConfig.getProperty("server.database.type");
            final var folderPath = FileUtils.pathJoining(
                    CommonConstant.ROOT_DIRECTORY_PROJECT_PATH,
                    "data",
                    "sql"
            );
            FileUtils.deleteFolder(new File(folderPath));
            createFolderIfNotExists(folderPath);
            final var postHandleComponent = new ArrayList<Class<?>>();
            final var handledPostHandleComponent = new ArrayList<Class<?>>();
            // createDatasource();
            ENTITIES.addAll(
                    listOfClassInPackage.stream()
                            .filter(aClass -> aClass.getAnnotation(Entity.class) != null ||
                                    aClass.getAnnotation(Table.class) != null)
                            .collect(Collectors.toCollection(ArrayList::new))
            );
            for (Class<?> aClass : listOfClassInPackage) {

                if (aClass.getAnnotation(TableName.class) != null) {
                    EntityUtils.analyzeEntityClass(aClass, sourceType.toLowerCase(), folderPath);
                    continue;
                }

                if (aClass.getAnnotation(Verticle.class) != null) {
                    verticleClass.add(aClass);
                    continue;
                }

                final var mapperClassAnnotation = aClass.getAnnotation(MapperClass.class);
                if (mapperClassAnnotation != null && aClass.isInterface()) {
                    CLASS_POOL.put(aClass.getName(), Mapper.getInstance(aClass));
                }
                final var instanceClassAnnotation = aClass.getAnnotation(InstanceClass.class);
                if (instanceClassAnnotation != null) {
                    final var methodsOfInstance = Arrays.stream(
                            aClass.getDeclaredMethods()
                    ).filter(m -> m.getReturnType() != Void.TYPE && m.getAnnotation(Instance.class) != null).collect(Collectors.toList());
                    if (!methodsOfInstance.isEmpty()) {
                        final var instanceClass = aClass.getDeclaredConstructor().newInstance();
                        for (Method method : methodsOfInstance) {
                            final var instanceMethodResult = method.invoke(instanceClass);
                            putInstanceToClassPool(instanceMethodResult.getClass(), instanceMethodResult);
                            CLASS_POOL.put(method.getName(), instanceMethodResult);
                            CLASS_POOL.put(method.getReturnType().getName(), instanceMethodResult);
                        }
                    }
                    continue;
                }
                final var componentAnnotation = aClass.getAnnotation(Component.class);
                if (componentAnnotation != null) {
                    final var fieldsOfComponent = Arrays.stream(aClass.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList());
                    if (fieldsOfComponent.isEmpty()) {
                        LogUtils.writeLog(LogUtils.Level.DEBUG, "Creating instance for {}", aClass);
                        final var instance = aClass.getDeclaredConstructor().newInstance();
                        if (!checkProxy(instance)) {
                            putInstanceToClassPool(aClass, instance);
                        }
                        handlePostConstructMethod(aClass, instance);
                    } else {
                        postHandleComponent.add(aClass);
                    }
                }
            }

            // TODO this part is waiting for another method implementation
            var count = 0;
            final var limit = 10;
            while (postHandleComponent.size() != handledPostHandleComponent.size()) {
                if (limit == count) {
                    break;
                }
                boolean aClassHasNotBeenAddedToPool = true;
                for (Class<?> aClass : postHandleComponent) {
                    if (handledPostHandleComponent.stream().anyMatch(c -> c.isAssignableFrom(aClass))) {
                        continue;
                    }
                    final var fields = new ArrayList<Field>();
                    getFieldsOfClass(fields, aClass);
                    final var fieldsOfComponent = fields.stream().filter(f -> !Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())).collect(Collectors.toList());

                    // final Class<?>[] asd = getConstructorWithMostParameters(aClass).getParameterTypes();
                    final Class<?>[] fieldArr = fieldsOfComponent.stream().map(Field::getType).toArray(Class[]::new);

                    final Object[] args = fieldsOfComponent
                            .stream()
                            .map(ClassPool::getInstanceOfField)
                            .toArray(Object[]::new);
                    if (Arrays.stream(args).noneMatch(Objects::isNull)) {
                        LogUtils.writeLog(LogUtils.Level.DEBUG, "Creating instance for {}", aClass);
                        final var instance = aClass.getDeclaredConstructor(fieldArr).newInstance(args);
                        handlePostConstructMethod(aClass, instance);
                        if (!checkProxy(instance)) {
                            putInstanceToClassPool(aClass, instance);
                        }
                        handledPostHandleComponent.add(aClass);
                        aClassHasNotBeenAddedToPool = false;
                    }
                }
                if (aClassHasNotBeenAddedToPool) {
                    ++count;
                } else {
                    --count;
                }
            }
            if (limit == count && postHandleComponent.size() != handledPostHandleComponent.size()) {
                final var message = new StringBuilder("[");
                for (Class<?> aClass : postHandleComponent) {
                    if (handledPostHandleComponent.stream().noneMatch(handledClass -> handledClass.isAssignableFrom(aClass))) {
                        final var fields = new ArrayList<Field>();
                        getFieldsOfClass(fields, aClass);
                        final var fieldsOfComponent = fields.stream().filter(f -> !Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())).collect(Collectors.toList());
                        final var fieldNotCreated = fieldsOfComponent.stream()
                                .filter(
                                        field ->
                                                !CLASS_POOL.containsKey(field.getType().getName()) &&
                                                        !CLASS_POOL.containsKey(field.getName())
                                ).map(f -> f.getName() + ": " + f.getType().getName())
                                .collect(Collectors.toCollection(ArrayList::new));
                        message.append(
                                String.format(
                                        "Cannot found instance of fields (%s) of class %s",
                                        String.join(", ", fieldNotCreated),
                                        aClass.getName()
                                )
                        ).append(", ");
                    }
                }
                message.append("]");
                throw new ExceptionInInitializerError(message.toString());
            }
        } catch (Throwable e) {
            LoggerFactory.getLogger(ClassPool.class).error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void putInstanceToClassPool(Class<?> aClass, Object instance) {
        CLASS_POOL.put(aClass.getName(), instance);

        final var superClass = aClass.getSuperclass();

        if (superClass != null && superClass != Object.class) {
            ClassPool.CLASS_POOL.put(superClass.getName(), instance);
        }

        final var iFace = aClass.getInterfaces();

        for (Class<?> iFaceClass : iFace) {
            ClassPool.CLASS_POOL.put(iFaceClass.getName(), instance);
        }
    }

    private static boolean checkProxy(Object instance) {
        final var proxyClassName = instance.getClass().getName() + "Proxy";
        try {
            Class<?> proxyClass = Class.forName(proxyClassName);
            Object proxyInstance = proxyClass.getDeclaredConstructor(instance.getClass()).newInstance(instance);
            var superClasses = ObjectUtils.getExtendAndInterfaceClasses(proxyClass);
            setInstance(proxyInstance);
            for (Class<?> superClass : superClasses) {
                setInstance(superClass.getName(), proxyInstance);
            }
            return true;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException ignore) {
        }
        return false;
    }

    private static Object getInstanceOfField(Field field) {
        Object o1 = CLASS_POOL.get(field.getName());
        if (o1 != null) {
            return o1;
        }
        return CLASS_POOL.get(field.getType().getName());
    }

    private static void getFieldsOfClass(final ArrayList<Field> fields, Class<?> aClass) {
        if (aClass.getSuperclass() != null) {
            List<Field> superClassField = Arrays.asList(aClass.getSuperclass().getDeclaredFields());
            fields.addAll(superClassField);
        }
        fields.addAll(Arrays.asList(aClass.getDeclaredFields()));
    }

    private static void createDatasource() {
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
        String driverClassName = CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.driver_class_name");
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
        if (host.equals(CommonConstant.NULL_STRING) || username.equals(CommonConstant.NULL_STRING) || password.equals(CommonConstant.NULL_STRING) || name.equals(CommonConstant.NULL_STRING) ||
                // driverClassName.equals(CommonConstant.NULL_STRING) ||
                port == 0 || initialPoolSize == 0 || maxPoolSize == 0 || maxTimeout == 0 || type == null) {
            return;
        }
        boolean hikari = Boolean.parseBoolean(CommonConstant.EMPTY_STRING + applicationConfig.getPropertyWithEnvironment("server.database.hikari"));
        LCXDataSource dataSource;
        if (hikari) {
            dataSource = HikariLcxDataSource.init(host, port, username, password, name, driverClassName, initialPoolSize, maxPoolSize, maxTimeout, type);
        } else {
            dataSource = LCXDataSource.init(host, port, username, password, name, driverClassName, initialPoolSize, maxPoolSize, maxTimeout, type);
        }
        CLASS_POOL.put(LCXDataSource.class.getName(), dataSource);
    }

    public static void handlePostConstructMethod(Class<?> aClass, Object instance) throws Exception {

        final var postConstructMethods = Arrays.stream(aClass.getDeclaredMethods()).filter(m -> m.getAnnotation(PostConstruct.class) != null).collect(Collectors.toList());
        final var hasMoreThanOnePostConstructMethod = postConstructMethods.size() > 1;
        if (hasMoreThanOnePostConstructMethod) {
            throw new RuntimeException(
                    String.format(
                            "Cannot create instance of %s because there are more than one PostConstruct method",
                            aClass.getName()
                    )
            );
        }
        if (!postConstructMethods.isEmpty()) {
            final var postConstructMethod = postConstructMethods.get(0);

            if (postConstructMethod.getReturnType().equals(void.class)) {

                if (postConstructMethod.getParameterCount() > 0) {
                    throw new RuntimeException(
                            String.format(
                                    "Cannot create instance of %s. Does not accept parameters",
                                    aClass.getName()
                            )
                    );
                }

                postConstructMethod.invoke(instance);
            } else {
                throw new RuntimeException(
                        String.format(
                                "Post construct of %s must be a void method",
                                aClass.getName()
                        )
                );
            }
        }
    }

    public static Object getInstance(String name) {
        return CLASS_POOL.get(name);
    }

    public static <T> T getInstance(String name, Class<T> clazz) {
        return clazz.cast(CLASS_POOL.get(name));
    }

    public static <T> T getInstance(Class<T> clazz) {
        return clazz.cast(CLASS_POOL.get(clazz.getName()));
    }

    public static void setInstance(String name, Object instance) {
        CLASS_POOL.put(name, instance);
        CLASS_POOL.put(instance.getClass().getName(), instance);
        // final var iFaces = instance.getClass().getInterfaces();
        // for (Class<?> iFaceClass : iFaces) {
        //     CLASS_POOL.put(iFaceClass.getName(), instance);
        // }
    }

    public static void setInstance(Object instance) {
        CLASS_POOL.put(instance.getClass().getName(), instance);
        final var iFaces = instance.getClass().getInterfaces();
        for (Class<?> iFaceClass : iFaces) {
            CLASS_POOL.put(iFaceClass.getName(), instance);
        }
    }

    public static void loadProperties() {
        ClassLoader classLoader = ClassPool.class.getClassLoader();
        final String configFile = System.getProperty("application_config.file");
        if (configFile != null) {
            CommonConstant.applicationConfig = PropertiesUtils.getProperties(configFile);
        } else {
            CommonConstant.applicationConfig = PropertiesUtils.getProperties(classLoader, "application.yaml");
        }
    }

}
