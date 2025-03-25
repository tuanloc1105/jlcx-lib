package vn.com.lcx.common.utils;

import java.util.ArrayList;
import java.util.List;

public final class JVMSystemInfo {

    public JVMSystemInfo() {
    }

    /**
     * @return number of available thread
     */
    public static int availableThread() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static List<Long> memoryInfo() {
        final Runtime runtime = Runtime.getRuntime();
        // Total memory in the JVM (in bytes)
        long totalMemory = runtime.totalMemory();

        // Free memory available in the JVM (in bytes)
        long freeMemory = runtime.freeMemory();

        // Maximum memory the JVM will attempt to use (in bytes)
        long maxMemory = runtime.maxMemory();

        final List<Long> memoryInfo = new ArrayList<>();

        memoryInfo.add(totalMemory / (1024 * 1024));
        memoryInfo.add(freeMemory / (1024 * 1024));
        memoryInfo.add(maxMemory / (1024 * 1024));
        return memoryInfo;
    }

}
