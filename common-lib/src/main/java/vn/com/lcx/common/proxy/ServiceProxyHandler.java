package vn.com.lcx.common.proxy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import vn.com.lcx.common.utils.ObjectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.Callable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceProxyHandler<T> implements InvocationHandler {

    private final Object target;
    private final boolean interfaze;

    /**
     * Creates a proxy instance for the given interface type and target object.
     * If the interface type is an interface, it uses the {@link Proxy} class to create the proxy.
     * Otherwise, it uses the {@link ByteBuddy} to create a subclass that delegates all method calls to the target object.
     * The created proxy will intercept all method calls to the target object and will handle the transaction handling and connection pooling.
     *
     * @param interfaceType the interface type of the proxy
     * @param target        the target object
     * @param <T>           the type of the interface
     * @return the created proxy instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceType, Object target) {
        if (interfaceType.isInterface()) {
            return (T) Proxy.newProxyInstance(
                    interfaceType.getClassLoader(),
                    new Class<?>[]{interfaceType},
                    new ServiceProxyHandler<>(target, true)
            );
        }
        try {
            // Create a subclass that delegates all method calls to the target object
            return (T) new ByteBuddy()
                    .subclass(interfaceType)
                    .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                    .intercept(MethodDelegation.to(new ServiceProxyHandler<T>(target, false)))
                    .make()
                    .load(interfaceType.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (NoSuchMethodException e) {
            // If no default constructor is available, try to find a suitable constructor
            try {
                // Find all constructors and try to create instance with default values
                Constructor<?>[] constructors = interfaceType.getDeclaredConstructors();
                if (constructors.length > 0) {
                    Constructor<?> constructor = constructors[0];
                    Object[] initArgs = new Object[constructor.getParameterCount()];
                    // Initialize with default values
                    for (int i = 0; i < initArgs.length; i++) {
                        Class<?> type = constructor.getParameterTypes()[i];
                        initArgs[i] = ObjectUtils.getDefaultValue(type);
                    }

                    // Create a new instance with default values
                    return (T) new ByteBuddy()
                            .subclass(interfaceType)
                            .method(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
                            .intercept(MethodDelegation.to(new ServiceProxyHandler<T>(target, false)))
                            .make()
                            .load(interfaceType.getClassLoader())
                            .getLoaded()
                            .getDeclaredConstructor(constructor.getParameterTypes())
                            .newInstance(initArgs);
                }
                throw new RuntimeException("No constructors found for " + interfaceType.getName(), e);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create class proxy for " + interfaceType.getName() +
                        ". Class must have at least one accessible constructor.", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create class proxy for " + interfaceType.getName(), e);
        }
    }

    @RuntimeType
    public Object intercept(@Origin Method method,
                            @AllArguments Object[] args,
                            @SuperCall Callable<?> callable) throws Throwable {
        try {
            return invoke(null, method, args);
        } catch (Exception e) {
            throw e.getCause() != null ? e.getCause() : e;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle Object methods
        switch (method.getName()) {
            case "toString":
                return "Proxy for " + target.getClass().getName();
            case "hashCode":
                return System.identityHashCode(proxy);
            case "equals":
                return proxy == args[0];
        }
        // Method implementMethod = getActualMethod(method, args);

        // TODO: implement a transaction management at here

        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getCause();
            if (targetException instanceof RuntimeException || targetException instanceof Error) {
                throw targetException;
            }
            throw new RuntimeException("Exception in method " + method.getName(), targetException);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error invoking method " + method.getName(), e);
        }
    }

    /**
     * Gets the actual method on the target object given the method from the interface.
     * It filters the methods on the target object by name, and then by parameter length.
     * If the parameter length is the same, then it further filters by parameter type.
     * If there is no valid method, then it throws a runtime exception.
     *
     * @param method the method from the interface
     * @param args   the arguments of the method
     * @return the actual method on the target object
     */
    private Method getActualMethod(Method method, Object[] args) {
        return Arrays.stream(target.getClass().getMethods())
                .filter(
                        m -> {
                            if (!m.getName().equals(method.getName())) {
                                return false;
                            }
                            if (args == null) {
                                return true;
                            }
                            if (m.getParameterCount() != args.length) {
                                return false;
                            }
                            for (int i = 0; i < args.length; i++) {
                                if (args[i] == null) {
                                    continue;
                                }
                                Class<?> type1 = ObjectUtils.wrapPrimitive(m.getParameterTypes()[i]);
                                Class<?> type2 = ObjectUtils.wrapPrimitive(args[i].getClass());
                                if (!type1.isAssignableFrom(type2)) {
                                    return false;
                                }
                            }
                            return true;
                        }
                )
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find valid method"));
    }

}
