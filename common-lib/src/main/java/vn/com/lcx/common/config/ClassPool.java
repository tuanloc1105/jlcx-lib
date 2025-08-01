package vn.com.lcx.common.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.annotation.PostConstruct;
import vn.com.lcx.common.annotation.TableName;
import vn.com.lcx.common.annotation.Verticle;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.utils.EntityUtils;
import vn.com.lcx.common.exception.DuplicateInstancesException;
import vn.com.lcx.common.scanner.PackageScanner;
import vn.com.lcx.common.utils.FileUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.ObjectUtils;
import vn.com.lcx.common.utils.PropertiesUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static vn.com.lcx.common.utils.FileUtils.createFolderIfNotExists;

public class ClassPool {

    public static final List<Class<?>> ENTITIES = new ArrayList<>();
    private static final ConcurrentHashMap<String, Object> CLASS_POOL = new ConcurrentHashMap<>();

    public static void init(final List<String> packagesToScan, final List<Class<?>> verticleClass) {
        packagesToScan.add("vn.com.lcx");
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

            var sourceType = CommonConstant.applicationConfig.getPropertyWithEnvironment("server.database.type");
            if (sourceType.equals(CommonConstant.NULL_STRING)) {
                sourceType = CommonConstant.applicationConfig.getPropertyWithEnvironment("server.reactive.database.type");
            }
            final var folderPath = FileUtils.pathJoining(
                    CommonConstant.ROOT_DIRECTORY_PROJECT_PATH,
                    "data",
                    "sql"
            );
            FileUtils.deleteFolder(new File(folderPath));
            createFolderIfNotExists(folderPath);
            final var postHandleComponent = new ArrayList<Class<?>>();
            final var handledPostHandleComponent = new ArrayList<Class<?>>();
            ENTITIES.addAll(
                    listOfClassInPackage.stream()
                            .filter(aClass -> aClass.getAnnotation(Entity.class) != null ||
                                    aClass.getAnnotation(Table.class) != null)
                            .collect(Collectors.toCollection(ArrayList::new))
            );
            for (Class<?> aClass : listOfClassInPackage) {

                if (aClass.getAnnotation(TableName.class) != null) {
                    EntityUtils.analyzeEntityClassV2(aClass, sourceType.toLowerCase(), folderPath);
                    continue;
                }

                if (aClass.getAnnotation(Verticle.class) != null) {
                    verticleClass.add(aClass);
                    continue;
                }
                final var componentAnnotation = aClass.getAnnotation(Component.class);
                if (componentAnnotation != null) {
                    final var fieldsOfComponent = Arrays.stream(aClass.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList());
                    if (fieldsOfComponent.isEmpty()) {
                        LogUtils.writeLog(LogUtils.Level.DEBUG, "Creating instance for {}", aClass);
                        final var instance = aClass.getDeclaredConstructor().newInstance();
                        if (!checkProxy(instance)) {
                            setInstance(instance);
                        }
                        createInstancesAndHandlePostConstructMethod(aClass, instance);
                    } else {
                        postHandleComponent.add(aClass);
                    }
                }
            }

            var count = 0;
            final var limit = 1;
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

                    if (aClass.getDeclaredConstructors().length > 1) {
                        throw new ExceptionInInitializerError(String.format("Class `%s` should have only 1 constructor", aClass));
                    }

                    final Class<?>[] fieldArr = getConstructorParameters(aClass.getDeclaredConstructors()[0]);

                    final Object[] args = fieldsOfComponent
                            .stream()
                            .map(ClassPool::getInstanceOfField)
                            .toArray(Object[]::new);
                    if (Arrays.stream(args).noneMatch(Objects::isNull)) {
                        LogUtils.writeLog(LogUtils.Level.DEBUG, "Creating instance for {}", aClass);
                        final var instance = aClass.getDeclaredConstructor(fieldArr).newInstance(args);
                        createInstancesAndHandlePostConstructMethod(aClass, instance);
                        if (!checkProxy(instance)) {
                            setInstance(instance);
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

    public static void createInstancesAndHandlePostConstructMethod(Class<?> aClass, Object instance) throws Exception {
        final var methodsOfInstance = Arrays.stream(
                aClass.getDeclaredMethods()
        ).filter(m -> m.getReturnType() != Void.TYPE && m.getAnnotation(Instance.class) != null).collect(Collectors.toList());
        if (!methodsOfInstance.isEmpty()) {
            for (Method method : methodsOfInstance) {
                final var instanceMethodResult = method.invoke(instance);
                if (instanceMethodResult != null) {
                    setInstance(instanceMethodResult);
                    final var instanceName = Optional.ofNullable(method.getAnnotation(Instance.class))
                            .filter(it ->
                                    StringUtils.isNotBlank(it.value())
                            ).map(Instance::value)
                            .orElse(CommonConstant.EMPTY_STRING);
                    if (StringUtils.isNotBlank(instanceName)) {
                        setInstance(instanceName, instanceMethodResult);
                    } else {
                        setInstance(method.getName(), instanceMethodResult);
                    }
                }
            }
        }
        final var postConstructMethods = Arrays
                .stream(aClass.getDeclaredMethods())
                .filter(m -> m.getAnnotation(PostConstruct.class) != null)
                .collect(Collectors.toList());
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
        final var existingInstance = CLASS_POOL.get(name);
        if (existingInstance != null) {
            throw new DuplicateInstancesException(
                    String.format(
                            "An instance with name %s already existed with type %s",
                            name,
                            existingInstance.getClass().getName()
                    )
            );
        }
        set(name, instance);
        setInstance(instance);
    }

    public static void setInstance(Object instance) {
        set(instance.getClass().getName(), instance);
        set(instance.getClass().getSimpleName(), instance);
        final var superClasses = ObjectUtils.getExtendAndInterfaceClasses(instance.getClass());
        for (Class<?> superClass : superClasses) {
            set(superClass.getName(), instance);
            set(superClass.getSimpleName(), instance);
        }
        final var superClass = instance.getClass().getSuperclass();
        if (superClass != null && superClass != Object.class) {
            set(superClass.getName(), instance);
            set(superClass.getSimpleName(), instance);
        }
        final var iFaces = instance.getClass().getInterfaces();
        for (Class<?> iFaceClass : iFaces) {
            set(iFaceClass.getName(), instance);
            set(iFaceClass.getSimpleName(), instance);
        }
    }

    private static void set(String name, Object instance) {
        if (CLASS_POOL.get(name) == null) {
            CLASS_POOL.put(name, instance);
            return;
        }
        int count = 1;
        while (CLASS_POOL.get(name + count) != null) {
            count++;
        }
        CLASS_POOL.put(name + count, instance);
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean checkProxy(Object instance) {
        final var proxyClassName = instance.getClass().getName() + "Proxy";
        try {
            Class<?> proxyClass = Class.forName(proxyClassName);
            Object proxyInstance = proxyClass.getDeclaredConstructor(instance.getClass()).newInstance(instance);
            setInstance(proxyInstance);
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

    private static Class<?>[] getConstructorParameters(Constructor<?> constructor) {
        return constructor.getParameterTypes();
    }

}
