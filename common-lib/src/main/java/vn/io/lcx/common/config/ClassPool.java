package vn.io.lcx.common.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import vn.io.lcx.common.annotation.Component;
import vn.io.lcx.common.annotation.DependsOn;
import vn.io.lcx.common.annotation.Instance;
import vn.io.lcx.common.annotation.PostConstruct;
import vn.io.lcx.common.annotation.Qualifier;
import vn.io.lcx.common.annotation.TableName;
import vn.io.lcx.common.annotation.Verticle;
import vn.io.lcx.common.constant.CommonConstant;
import vn.io.lcx.common.database.utils.EntityUtils;
import vn.io.lcx.common.exception.DuplicateInstancesException;
import vn.io.lcx.common.scanner.PackageScanner;
import vn.io.lcx.common.utils.FileUtils;
import vn.io.lcx.common.utils.JsonMaskingUtils;
import vn.io.lcx.common.utils.LogUtils;
import vn.io.lcx.common.utils.ObjectUtils;
import vn.io.lcx.common.utils.PropertiesUtils;
import vn.io.lcx.reactive.context.EntityMappingContainer;
import vn.io.lcx.reactive.entity.EntityMapping;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static vn.io.lcx.common.utils.FileUtils.createFolderIfNotExists;

/**
 * Lightweight, reflection-based dependency injection container.
 *
 * <p>Responsible for scanning packages, discovering {@link Component @Component} classes,
 * and managing their lifecycle: instantiation, dependency resolution, and registration.</p>
 *
 * <h3>Initialization flow ({@link #init}):</h3>
 * <ol>
 *   <li><b>Package scanning</b> - discovers all classes via {@link vn.io.lcx.common.scanner.PackageScanner}.</li>
 *   <li><b>Entity registration</b> - collects {@code @Entity}/{@code @Table} classes and
 *       processes {@code @TableName} classes.</li>
 *   <li><b>Phase 1</b> - instantiates simple {@code @Component} classes (no non-static fields)
 *       via no-arg constructor.</li>
 *   <li><b>Phase 2</b> - iteratively resolves dependent {@code @Component} classes via
 *       constructor injection, and resolves deferred {@link Instance @Instance} methods
 *       whose parameters were not yet available.</li>
 *   <li><b>Error reporting</b> - throws {@link ExceptionInInitializerError} if any component
 *       or {@code @Instance} method remains unresolved.</li>
 * </ol>
 *
 * <h3>Instance registration:</h3>
 * <p>Each instance is registered under multiple keys for polymorphic lookup:</p>
 * <ul>
 *   <li>Fully qualified class name and simple class name</li>
 *   <li>All superclass names and interface names</li>
 *   <li>Custom name via {@link Instance#value()} or method name</li>
 * </ul>
 * <p>Naming conflicts are resolved by appending numeric suffixes (e.g. {@code name1}, {@code name2}).</p>
 *
 * <h3>Dependency resolution order (constructor parameters / {@code @Instance} method parameters):</h3>
 * <ol>
 *   <li>{@link Qualifier @Qualifier} on the parameter itself</li>
 *   <li>Fallback to matching field's {@code @Qualifier} (for constructor params, supports Lombok)</li>
 *   <li>Parameter name</li>
 *   <li>Parameter type name</li>
 * </ol>
 *
 * @see Component
 * @see Instance
 * @see Qualifier
 * @see PostConstruct
 */
public class ClassPool {

    private record DeferredInstanceMethod(Object componentInstance, Method method) {}

    private static final List<Class<?>> ENTITIES = new ArrayList<>();
    private static final ConcurrentHashMap<String, Object> CLASS_POOL = new ConcurrentHashMap<>();

    /**
     * Returns an unmodifiable list of all discovered entity classes
     * (annotated with {@code @Entity} or {@code @Table}).
     *
     * @return unmodifiable list of entity classes
     */
    public static List<Class<?>> getEntities() {
        return Collections.unmodifiableList(ENTITIES);
    }

    /**
     * Scans the given packages (plus {@code vn.io.lcx}), discovers components, entities,
     * and verticles, then instantiates and registers all {@link Component @Component} classes.
     *
     * <p>Components are created in two phases: simple components first, then dependent
     * components via iterative constructor injection. Deferred {@link Instance @Instance}
     * methods are also resolved in the iterative phase.</p>
     *
     * @param packagesToScan list of base packages to scan for classes
     * @param verticleClass  mutable list that will be populated with discovered {@link Verticle @Verticle} classes
     * @throws ExceptionInInitializerError if any component or {@code @Instance} method cannot be resolved
     */
    public static void init(final List<String> packagesToScan, final List<Class<?>> verticleClass) {
        final var allPackagesToScan = new ArrayList<>(packagesToScan);
        allPackagesToScan.add("vn.io.lcx");
        try {
            final List<Class<?>> listOfClassInPackage = new ArrayList<>();
            allPackagesToScan.forEach(packageName -> {
                try {
                    listOfClassInPackage.addAll(PackageScanner.findClasses(packageName));
                } catch (Exception e) {
                    LogUtils.writeLog(ClassPool.class, LogUtils.Level.WARN, "Failed to scan package: {}", packageName);
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
            final var deferredInstanceMethods = new ArrayList<DeferredInstanceMethod>();
            final var handledDeferredMethods = new ArrayList<DeferredInstanceMethod>();
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

                if (EntityMapping.class.isAssignableFrom(aClass) && !aClass.isInterface()) {
                    EntityMappingContainer.addMapping(aClass);
                    continue;
                }

                if (aClass.getAnnotation(Verticle.class) != null) {
                    verticleClass.add(aClass);
                    // continue;
                }
                final var componentAnnotation = aClass.getAnnotation(Component.class);
                if (componentAnnotation != null) {
                    final var fieldsOfComponent = Arrays.stream(aClass.getDeclaredFields()).filter(f -> !Modifier.isStatic(f.getModifiers())).toList();
                    if (fieldsOfComponent.isEmpty() && areDependsOnBeansSatisfied(aClass)) {
                        LogUtils.writeLog(ClassPool.class, LogUtils.Level.DEBUG, "Creating instance for {}", aClass);
                        final var instance = aClass.getDeclaredConstructor().newInstance();
                        if (!checkProxy(instance)) {
                            setInstance(instance);
                        }
                        createInstancesAndHandlePostConstructMethod(aClass, instance, deferredInstanceMethods);
                    } else {
                        postHandleComponent.add(aClass);
                    }
                }
            }

            boolean progress = true;
            while (progress && (postHandleComponent.size() != handledPostHandleComponent.size() || deferredInstanceMethods.size() != handledDeferredMethods.size())) {
                progress = false;
                for (Class<?> aClass : postHandleComponent) {
                    if (handledPostHandleComponent.contains(aClass)) {
                        continue;
                    }
                    if (!areDependsOnBeansSatisfied(aClass)) {
                        continue;
                    }
                    if (aClass.getDeclaredConstructors().length > 1) {
                        throw new ExceptionInInitializerError(String.format("Class `%s` must have only 1 constructor", aClass));
                    }

                    final var constructor = aClass.getDeclaredConstructors()[0];
                    final Class<?>[] constructorParamTypes = getConstructorParameters(constructor);
                    final Object[] args = resolveConstructorArgs(constructor, aClass);
                    if (Arrays.stream(args).noneMatch(Objects::isNull)) {
                        LogUtils.writeLog(ClassPool.class, LogUtils.Level.DEBUG, "Creating instance for {}", aClass);
                        final var instance = aClass.getDeclaredConstructor(constructorParamTypes).newInstance(args);
                        createInstancesAndHandlePostConstructMethod(aClass, instance, deferredInstanceMethods);
                        if (!checkProxy(instance)) {
                            setInstance(instance);
                        }
                        handledPostHandleComponent.add(aClass);
                        progress = true;
                    }
                }
                for (DeferredInstanceMethod deferred : deferredInstanceMethods) {
                    if (handledDeferredMethods.contains(deferred)) {
                        continue;
                    }
                    if (!areDependsOnBeansSatisfied(deferred.method())) {
                        continue;
                    }
                    final Object[] args = Arrays.stream(deferred.method().getParameters())
                            .map(ClassPool::getInstanceOfParameter)
                            .toArray(Object[]::new);
                    if (Arrays.stream(args).noneMatch(Objects::isNull)) {
                        invokeAndRegisterInstanceMethod(deferred.componentInstance(), deferred.method(), args);
                        handledDeferredMethods.add(deferred);
                        progress = true;
                    }
                }
            }
            if (postHandleComponent.size() != handledPostHandleComponent.size() || deferredInstanceMethods.size() != handledDeferredMethods.size()) {
                final var message = new StringBuilder("[");
                for (Class<?> aClass : postHandleComponent) {
                    if (!handledPostHandleComponent.contains(aClass)) {
                        final var reasons = new ArrayList<String>();
                        if (!areDependsOnBeansSatisfied(aClass)) {
                            reasons.add("unsatisfied @DependsOn: " + getMissingDependsOnDescription(aClass));
                        }
                        final var fields = new ArrayList<Field>();
                        getFieldsOfClass(fields, aClass);
                        final var fieldsOfComponent = fields.stream().filter(f -> !Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())).toList();
                        final var fieldNotCreated = fieldsOfComponent.stream()
                                .filter(
                                        field ->
                                                !CLASS_POOL.containsKey(field.getType().getName()) &&
                                                        !CLASS_POOL.containsKey(field.getName())
                                ).map(f -> f.getName() + ": " + f.getType().getName())
                                .collect(Collectors.toCollection(ArrayList::new));
                        if (!fieldNotCreated.isEmpty()) {
                            reasons.add("missing fields: " + String.join(", ", fieldNotCreated));
                        }
                        message.append(
                                String.format(
                                        "Cannot create instance of class %s [%s]",
                                        aClass.getName(),
                                        String.join("; ", reasons)
                                )
                        ).append(",\n");
                    }
                }
                for (DeferredInstanceMethod deferred : deferredInstanceMethods) {
                    if (!handledDeferredMethods.contains(deferred)) {
                        final var reasons = new ArrayList<String>();
                        if (!areDependsOnBeansSatisfied(deferred.method())) {
                            reasons.add("unsatisfied @DependsOn: " + getMissingDependsOnDescription(deferred.method()));
                        }
                        final var unresolvedParams = Arrays.stream(deferred.method().getParameters())
                                .filter(p -> getInstanceOfParameter(p) == null)
                                .map(p -> p.getName() + ": " + p.getType().getName())
                                .collect(Collectors.joining(", "));
                        if (!unresolvedParams.isEmpty()) {
                            reasons.add("unresolved parameters: " + unresolvedParams);
                        }
                        message.append(
                                String.format(
                                        "Cannot resolve @Instance method `%s` in class %s [%s]",
                                        deferred.method().getName(),
                                        deferred.method().getDeclaringClass().getName(),
                                        String.join("; ", reasons)
                                )
                        ).append(",\n");
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

    /**
     * Processes {@link Instance @Instance} factory methods and {@link PostConstruct @PostConstruct}
     * methods on the given component instance.
     *
     * <p>For each {@code @Instance} method:</p>
     * <ul>
     *   <li>No parameters: invoked immediately and the result is registered in the pool.</li>
     *   <li>Has parameters, all resolvable: invoked immediately with resolved beans.</li>
     *   <li>Has parameters, some unresolvable: added to {@code deferredMethods} for later retry.</li>
     * </ul>
     *
     * <p>After processing {@code @Instance} methods, the single {@code @PostConstruct} method
     * (if present) is invoked.</p>
     *
     * @param aClass          the component class
     * @param instance        the component instance
     * @param deferredMethods mutable list to collect {@code @Instance} methods with unresolved parameters
     * @throws Exception if method invocation fails or constraints are violated
     */
    public static void createInstancesAndHandlePostConstructMethod(Class<?> aClass, Object instance, List<DeferredInstanceMethod> deferredMethods) throws Exception {
        final var methodsOfInstance = Arrays.stream(
                aClass.getDeclaredMethods()
        ).filter(m -> m.getReturnType() != Void.TYPE && m.getAnnotation(Instance.class) != null).toList();
        if (!methodsOfInstance.isEmpty()) {
            for (Method method : methodsOfInstance) {
                if (!areDependsOnBeansSatisfied(method)) {
                    deferredMethods.add(new DeferredInstanceMethod(instance, method));
                } else if (method.getParameterCount() == 0) {
                    invokeAndRegisterInstanceMethod(instance, method);
                } else {
                    final Object[] args = Arrays.stream(method.getParameters())
                            .map(ClassPool::getInstanceOfParameter)
                            .toArray(Object[]::new);
                    if (Arrays.stream(args).noneMatch(Objects::isNull)) {
                        invokeAndRegisterInstanceMethod(instance, method, args);
                    } else {
                        deferredMethods.add(new DeferredInstanceMethod(instance, method));
                    }
                }
            }
        }
        final var postConstructMethods = Arrays
                .stream(aClass.getDeclaredMethods())
                .filter(m -> m.getAnnotation(PostConstruct.class) != null)
                .toList();
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

    /**
     * Retrieves a registered instance by name.
     *
     * @param name the registered name (class name, simple name, or custom name)
     * @return the instance, or {@code null} if not found
     */
    public static Object getInstance(String name) {
        return CLASS_POOL.get(name);
    }

    /**
     * Retrieves a registered instance by name, cast to the specified type.
     *
     * @param name  the registered name
     * @param clazz the expected type
     * @param <T>   the type
     * @return the instance cast to {@code T}, or {@code null} if not found
     * @throws ClassCastException if the instance is not assignable to {@code clazz}
     */
    public static <T> T getInstance(String name, Class<T> clazz) {
        return clazz.cast(CLASS_POOL.get(name));
    }

    /**
     * Retrieves a registered instance by its class type.
     * Looks up using the fully qualified class name.
     *
     * @param clazz the class type to look up
     * @param <T>   the type
     * @return the instance cast to {@code T}, or {@code null} if not found
     * @throws ClassCastException if the instance is not assignable to {@code clazz}
     */
    public static <T> T getInstance(Class<T> clazz) {
        return clazz.cast(CLASS_POOL.get(clazz.getName()));
    }

    /**
     * Registers an instance under a specific name. Also registers under the type hierarchy
     * via {@link #setInstance(Object)}.
     *
     * @param name     the name to register under
     * @param instance the instance to register
     * @throws DuplicateInstancesException if an instance with the same name already exists
     */
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

    /**
     * Registers an instance under its full type hierarchy: class name, simple name,
     * superclass names, and interface names. If a key already exists, a numeric suffix
     * is appended (e.g. {@code name1}, {@code name2}).
     *
     * @param instance the instance to register
     */
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
        if (CLASS_POOL.putIfAbsent(name, instance) == null) {
            return;
        }
        int count = 1;
        while (CLASS_POOL.putIfAbsent(name + count, instance) != null) {
            count++;
        }
    }

    /**
     * Loads application configuration from {@code application.yaml} (or from the file specified
     * by the {@code application_config.file} system property) and initializes JSON sensitive
     * field masking.
     */
    public static void loadProperties() {
        ClassLoader classLoader = ClassPool.class.getClassLoader();
        final String configFile = System.getProperty("application_config.file");
        if (configFile != null) {
            CommonConstant.applicationConfig = PropertiesUtils.getProperties(configFile);
        } else {
            CommonConstant.applicationConfig = PropertiesUtils.getProperties(classLoader, "application.yaml");
        }
        final List<String> fields = CommonConstant.applicationConfig.getProperty_("json.sensitive_field");
        if (fields != null && !fields.isEmpty()) {
            JsonMaskingUtils.CUSTOM_FIELD.addAll(fields);
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
        } catch (ClassNotFoundException ignore) {
            // Expected - no proxy class exists
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            LogUtils.writeLog(ClassPool.class, LogUtils.Level.WARN, "Failed to create proxy for: {}", instance.getClass().getName());
        }
        return false;
    }

    private static String getMissingDependsOnDescription(AnnotatedElement element) {
        DependsOn dependsOn = element.getAnnotation(DependsOn.class);
        if (dependsOn == null) return "";
        final var missing = new ArrayList<String>();
        for (String name : dependsOn.value()) {
            if (getInstance(name) == null) missing.add("\"" + name + "\"");
        }
        for (Class<?> clazz : dependsOn.classes()) {
            if (getInstance(clazz) == null) missing.add(clazz.getName());
        }
        return String.join(", ", missing);
    }

    private static boolean areDependsOnBeansSatisfied(AnnotatedElement element) {
        DependsOn dependsOn = element.getAnnotation(DependsOn.class);
        if (dependsOn == null) return true;
        for (String name : dependsOn.value()) {
            if (getInstance(name) == null) return false;
        }
        for (Class<?> clazz : dependsOn.classes()) {
            if (getInstance(clazz) == null) return false;
        }
        return true;
    }

    private static Object[] resolveConstructorArgs(Constructor<?> constructor, Class<?> componentClass) {
        final var params = constructor.getParameters();
        final var fields = new ArrayList<Field>();
        getFieldsOfClass(fields, componentClass);
        final var nonStaticFields = fields.stream()
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toList();
        final Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            final boolean hasMatchingField = i < nonStaticFields.size()
                    && nonStaticFields.get(i).getType().equals(params[i].getType());
            // 1. @Qualifier on parameter
            if (params[i].getAnnotation(Qualifier.class) != null) {
                args[i] = getInstance(params[i].getAnnotation(Qualifier.class).value());
            }
            // 2. @Qualifier on corresponding field (Lombok compatibility)
            if (args[i] == null && hasMatchingField
                    && nonStaticFields.get(i).getAnnotation(Qualifier.class) != null) {
                args[i] = getInstance(nonStaticFields.get(i).getAnnotation(Qualifier.class).value());
            }
            // 3. Parameter name
            if (args[i] == null) {
                args[i] = getInstance(params[i].getName());
            }
            // 4. Field name
            if (args[i] == null && hasMatchingField) {
                args[i] = getInstance(nonStaticFields.get(i).getName());
            }
            // 5. Type (last resort)
            if (args[i] == null) {
                args[i] = getInstance(params[i].getType().getName());
            }
        }
        return args;
    }

    private static Object getInstanceOfParameter(Parameter param) {
        if (param.getAnnotation(Qualifier.class) != null) {
            return getInstance(param.getAnnotation(Qualifier.class).value());
        } else {
            Object o1 = getInstance(param.getName());
            if (o1 != null) {
                return o1;
            }
            return getInstance(param.getType().getName());
        }
    }

    private static void invokeAndRegisterInstanceMethod(Object instance, Method method, Object... args) throws Exception {
        final var instanceMethodResult = method.invoke(instance, args);
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

    private static Object getInstanceOfField(Field field) {
        if (field.getAnnotation(Qualifier.class) != null) {
            return getInstance(field.getAnnotation(Qualifier.class).value());
        } else {
            Object o1 = getInstance(field.getName());
            if (o1 != null) {
                return o1;
            }
            return getInstance(field.getType().getName());
        }
    }

    private static void getFieldsOfClass(final ArrayList<Field> fields, Class<?> aClass) {
        fields.addAll(Arrays.asList(aClass.getDeclaredFields()));
        if (aClass.getSuperclass() != null) {
            List<Field> superClassField = Arrays.asList(aClass.getSuperclass().getDeclaredFields());
            fields.addAll(superClassField);
        }
    }

    private static Class<?>[] getConstructorParameters(Constructor<?> constructor) {
        return constructor.getParameterTypes();
    }

}
