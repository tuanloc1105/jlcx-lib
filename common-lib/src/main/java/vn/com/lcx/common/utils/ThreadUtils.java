package vn.com.lcx.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for thread management and monitoring operations.
 *
 * <p>This class provides methods to:</p>
 * <ul>
 *   <li>Log information about all running threads</li>
 *   <li>Interrupt threads by name</li>
 *   <li>Get lists of running thread names</li>
 *   <li>Get detailed thread information including stack traces</li>
 * </ul>
 *
 * <p><strong>Warning:</strong> Thread interruption should be used carefully as it can
 * cause unexpected behavior in applications. Always ensure proper thread lifecycle management.</p>
 *
 * @author tuanloc1105
 * @since 1.0
 */
public final class ThreadUtils {

    private ThreadUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Logs information about all currently running threads.
     *
     * <p>This method logs the name and state of each thread in the JVM.
     * Useful for debugging and monitoring thread activity.</p>
     */
    public static void logAllThreadsRunning() {
        Map<Thread, StackTraceElement[]> allThreads = getAllThreadStackTraces();
        LogUtils.writeLog(LogUtils.Level.INFO, "Found {} running threads", allThreads.size());

        for (Thread thread : allThreads.keySet()) {
            LogUtils.writeLog(LogUtils.Level.INFO,
                    "Thread name: {} | State: {} | Priority: {}",
                    thread.getName(),
                    thread.getState(),
                    thread.getPriority());
        }
    }

    /**
     * Interrupts a thread by its name.
     *
     * <p>This method finds a thread with the specified name and calls {@link Thread#interrupt()}
     * on it. Note that interruption is a cooperative mechanism - the thread must check
     * its interrupted status to respond to the interruption.</p>
     *
     * @param threadName the name of the thread to interrupt
     * @return true if a thread with the specified name was found and interrupted, false otherwise
     * @throws IllegalArgumentException if threadName is null or empty
     */
    public static boolean interruptThreadByName(final String threadName) {
        if (threadName == null || threadName.trim().isEmpty()) {
            throw new IllegalArgumentException("Thread name cannot be null or empty");
        }

        Map<Thread, StackTraceElement[]> allThreads = getAllThreadStackTraces();

        for (Thread thread : allThreads.keySet()) {
            if (threadName.equals(thread.getName())) {
                thread.interrupt();
                LogUtils.writeLog(LogUtils.Level.INFO,
                        "Thread '{}' (State: {}) has been interrupted",
                        thread.getName(),
                        thread.getState());
                return true;
            }
        }

        LogUtils.writeLog(LogUtils.Level.WARN, "No thread found with name: {}", threadName);
        return false;
    }

    /**
     * Gets a list of names of all currently running threads.
     *
     * @return a list containing the names of all running threads
     */
    public static List<String> getAllRunningThreadNames() {
        Map<Thread, StackTraceElement[]> allThreads = getAllThreadStackTraces();
        return allThreads.keySet().stream()
                .map(Thread::getName)
                .collect(Collectors.toList());
    }

    /**
     * Gets detailed information about all running threads including their state and stack traces.
     *
     * <p>This method returns a formatted list of strings containing detailed information
     * about each thread including name, state, priority, and stack trace.</p>
     *
     * @return a list of formatted strings containing thread information
     */
    public static List<String> getAllRunningThreadsDetailedInfo() {
        Map<Thread, StackTraceElement[]> allThreads = getAllThreadStackTraces();

        List<String> result = new ArrayList<>();

        for (Thread thread : allThreads.keySet()) {
            StringBuilder threadInfo = new StringBuilder();

            // Basic thread information
            threadInfo.append(String.format("Thread: %s | State: %s | Priority: %d",
                    thread.getName(),
                    thread.getState(),
                    thread.getPriority()));

            // Stack trace information
            StackTraceElement[] stackTrace = thread.getStackTrace();
            if (stackTrace.length > 0) {
                threadInfo.append("\n  Stack Trace:");
                for (int i = 0; i < Math.min(stackTrace.length, 10); i++) { // Limit to first 10 frames
                    StackTraceElement element = stackTrace[i];
                    threadInfo.append(String.format("\n    %d: %s.%s(%s:%d)",
                            i,
                            element.getClassName(),
                            element.getMethodName(),
                            element.getFileName() != null ? element.getFileName() : "Unknown",
                            element.getLineNumber()));
                }
                if (stackTrace.length > 10) {
                    threadInfo.append(String.format("\n    ... and %d more frames", stackTrace.length - 10));
                }
            } else {
                threadInfo.append("\n  No stack trace available");
            }

            result.add(threadInfo.toString());
        }

        return result;
    }

    /**
     * Gets the number of currently running threads.
     *
     * @return the number of threads currently running in the JVM
     */
    public static int getThreadCount() {
        return getAllThreadStackTraces().size();
    }

    /**
     * Checks if a thread with the specified name is currently running.
     *
     * @param threadName the name of the thread to check
     * @return true if a thread with the specified name is running, false otherwise
     * @throws IllegalArgumentException if threadName is null or empty
     */
    public static boolean isThreadRunning(String threadName) {
        if (threadName == null || threadName.trim().isEmpty()) {
            throw new IllegalArgumentException("Thread name cannot be null or empty");
        }

        Map<Thread, StackTraceElement[]> allThreads = getAllThreadStackTraces();
        return allThreads.keySet().stream()
                .anyMatch(thread -> threadName.equals(thread.getName()));
    }

    /**
     * Gets information about a specific thread by name.
     *
     * @param threadName the name of the thread to get information about
     * @return a formatted string containing thread information, or null if thread not found
     * @throws IllegalArgumentException if threadName is null or empty
     */
    public static String getThreadInfo(String threadName) {
        if (threadName == null || threadName.trim().isEmpty()) {
            throw new IllegalArgumentException("Thread name cannot be null or empty");
        }

        Map<Thread, StackTraceElement[]> allThreads = getAllThreadStackTraces();

        for (Thread thread : allThreads.keySet()) {
            if (threadName.equals(thread.getName())) {
                StringBuilder info = new StringBuilder();
                info.append(String.format("Thread: %s\n", thread.getName()));
                info.append(String.format("State: %s\n", thread.getState()));
                info.append(String.format("Priority: %d\n", thread.getPriority()));
                info.append(String.format("Daemon: %s\n", thread.isDaemon()));
                info.append(String.format("Alive: %s\n", thread.isAlive()));

                StackTraceElement[] stackTrace = thread.getStackTrace();
                if (stackTrace.length > 0) {
                    info.append("Stack Trace:\n");
                    for (int i = 0; i < Math.min(stackTrace.length, 5); i++) {
                        StackTraceElement element = stackTrace[i];
                        info.append(String.format("  %d: %s.%s(%s:%d)\n",
                                i,
                                element.getClassName(),
                                element.getMethodName(),
                                element.getFileName() != null ? element.getFileName() : "Unknown",
                                element.getLineNumber()));
                    }
                }

                return info.toString();
            }
        }

        return null;
    }

    /**
     * Gets a map of all threads and their stack traces.
     *
     * @return a map where keys are Thread objects and values are their stack traces
     */
    private static Map<Thread, StackTraceElement[]> getAllThreadStackTraces() {
        return Thread.getAllStackTraces();
    }
}
