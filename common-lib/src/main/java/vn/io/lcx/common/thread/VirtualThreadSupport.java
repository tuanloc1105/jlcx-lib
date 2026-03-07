package vn.io.lcx.common.thread;

import vn.io.lcx.common.utils.LogUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Utility class for Virtual Thread support detection and creation.
 *
 * <p>This class uses reflection to detect and create Virtual Threads,
 * allowing the code to compile on JDK 17 while supporting Virtual Threads
 * when running on JDK 21 or later.</p>
 *
 * <p>Virtual Threads (Project Loom) are lightweight threads introduced in JDK 21
 * that significantly reduce the overhead of thread creation and context switching,
 * making them ideal for I/O-bound workloads with high concurrency.</p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * if (VirtualThreadSupport.isVirtualThreadSupported()) {
 *     ExecutorService executor = VirtualThreadSupport.newVirtualThreadPerTaskExecutor();
 *     // Use virtual thread executor
 * } else {
 *     // Fallback to platform threads
 * }
 * }</pre>
 *
 */
@SuppressWarnings("JavaReflectionMemberAccess")
public final class VirtualThreadSupport {

    private static final boolean VIRTUAL_THREAD_SUPPORTED;
    private static final Method NEW_VIRTUAL_THREAD_PER_TASK_EXECUTOR_METHOD;
    private static final Method OF_VIRTUAL_METHOD;

    static {
        boolean supported = false;
        Method executorMethod = null;
        Method ofVirtualMethod = null;

        try {
            // Try to find Executors.newVirtualThreadPerTaskExecutor() method (JDK 21+)
            executorMethod = Executors.class.getMethod("newVirtualThreadPerTaskExecutor");

            // Try to find Thread.ofVirtual() method (JDK 21+)
            ofVirtualMethod = Thread.class.getMethod("ofVirtual");

            supported = true;
            LogUtils.writeLog(
                    VirtualThreadSupport.class,
                    LogUtils.Level.INFO,
                    "Virtual Thread support detected (JDK 21+)"
            );
        } catch (NoSuchMethodException e) {
            LogUtils.writeLog(
                    VirtualThreadSupport.class,
                    LogUtils.Level.DEBUG,
                    "Virtual Thread not supported on current JDK version. Falling back to platform threads."
            );
        }

        VIRTUAL_THREAD_SUPPORTED = supported;
        NEW_VIRTUAL_THREAD_PER_TASK_EXECUTOR_METHOD = executorMethod;
        OF_VIRTUAL_METHOD = ofVirtualMethod;
    }

    private VirtualThreadSupport() {
        // Utility class, prevent instantiation
    }

    /**
     * Checks if Virtual Threads are supported on the current JDK.
     *
     * @return true if Virtual Threads are supported (JDK 21+), false otherwise
     */
    public static boolean isVirtualThreadSupported() {
        return VIRTUAL_THREAD_SUPPORTED;
    }

    /**
     * Creates a new Virtual Thread per task executor.
     *
     * <p>This method creates an ExecutorService that starts a new Virtual Thread
     * for each submitted task. Virtual Threads are lightweight and designed for
     * I/O-bound workloads.</p>
     *
     * <p>If Virtual Threads are not supported on the current JDK, this method
     * returns null. Callers should check {@link #isVirtualThreadSupported()}
     * before calling this method or handle the null return value appropriately.</p>
     *
     * @return a new Virtual Thread per task executor, or null if not supported
     */
    public static ExecutorService newVirtualThreadPerTaskExecutor() {
        if (!VIRTUAL_THREAD_SUPPORTED || NEW_VIRTUAL_THREAD_PER_TASK_EXECUTOR_METHOD == null) {
            return null;
        }

        try {
            return (ExecutorService) NEW_VIRTUAL_THREAD_PER_TASK_EXECUTOR_METHOD.invoke(null);
        } catch (Exception e) {
            LogUtils.writeLog(
                    VirtualThreadSupport.class,
                    LogUtils.Level.WARN,
                    "Failed to create Virtual Thread executor: {}",
                    e.getMessage()
            );
            return null;
        }
    }

    /**
     * Creates a new Virtual Thread per task executor with a custom thread name prefix.
     *
     * <p>This method creates an ExecutorService that starts a new Virtual Thread
     * for each submitted task, with threads named using the specified prefix.</p>
     *
     * <p>If Virtual Threads are not supported on the current JDK, this method
     * returns null.</p>
     *
     * @param namePrefix the prefix for virtual thread names
     * @return a new Virtual Thread per task executor with named threads, or null if not supported
     */
    public static ExecutorService newVirtualThreadPerTaskExecutor(String namePrefix) {
        if (!VIRTUAL_THREAD_SUPPORTED || OF_VIRTUAL_METHOD == null) {
            return null;
        }

        try {
            // Thread.ofVirtual().name(prefix, 0).factory()
            Object virtualBuilder = OF_VIRTUAL_METHOD.invoke(null);
            Class<?> builderClass = virtualBuilder.getClass();

            // Find the name(String, long) method
            Method nameMethod = findMethod(builderClass, "name", String.class, long.class);
            if (nameMethod != null) {
                nameMethod.setAccessible(true);
                virtualBuilder = nameMethod.invoke(virtualBuilder, namePrefix + "-", 0L);
            }

            // Find the factory() method
            Method factoryMethod = findMethod(builderClass, "factory");
            if (factoryMethod != null) {
                factoryMethod.setAccessible(true);
                ThreadFactory factory = (ThreadFactory) factoryMethod.invoke(virtualBuilder);

                // Use Executors.newThreadPerTaskExecutor(factory)
                Method newThreadPerTaskExecutorMethod = Executors.class.getMethod(
                        "newThreadPerTaskExecutor",
                        ThreadFactory.class
                );
                return (ExecutorService) newThreadPerTaskExecutorMethod.invoke(null, factory);
            }

            // Fallback to default virtual thread executor
            return newVirtualThreadPerTaskExecutor();
        } catch (Exception e) {
            LogUtils.writeLog(
                    VirtualThreadSupport.class,
                    LogUtils.Level.WARN,
                    "Failed to create named Virtual Thread executor, falling back to default: {}",
                    e.getMessage()
            );
            return newVirtualThreadPerTaskExecutor();
        }
    }

    /**
     * Gets the current JDK version's major number.
     *
     * @return the major version number of the current JDK
     */
    public static int getJdkMajorVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            // For JDK 8 and earlier: 1.8.0_xxx
            return Integer.parseInt(version.substring(2, 3));
        } else {
            // For JDK 9+: 9.0.x, 11.0.x, 17.0.x, 21.0.x
            int dotIndex = version.indexOf('.');
            if (dotIndex > 0) {
                return Integer.parseInt(version.substring(0, dotIndex));
            }
            // Handle versions like "21" without dots
            int dashIndex = version.indexOf('-');
            if (dashIndex > 0) {
                return Integer.parseInt(version.substring(0, dashIndex));
            }
            return Integer.parseInt(version);
        }
    }

    /**
     * Finds a method in the given class or its interfaces/superinterfaces.
     *
     * @param clazz      the class to search
     * @param methodName the method name
     * @param paramTypes the parameter types
     * @return the method if found, null otherwise
     */
    private static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // Try to find in declared methods
            try {
                return clazz.getDeclaredMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                // Search in interfaces
                for (Class<?> iface : clazz.getInterfaces()) {
                    Method method = findMethod(iface, methodName, paramTypes);
                    if (method != null) {
                        return method;
                    }
                }
                // Search in superclass
                Class<?> superClass = clazz.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    return findMethod(superClass, methodName, paramTypes);
                }
            }
        }
        return null;
    }
}
