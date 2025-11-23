package vn.com.lcx.common.utils;

import io.vertx.core.Vertx;
import vn.com.lcx.vertx.base.custom.EmptyRoutingContext;

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

    public static void printMemoryUsage(Vertx vertx) {
        long period = 6 * 60 * 60 * 1000; // 21,600,000 milliseconds (6 hours)
        long now = System.currentTimeMillis();
        long nextMinuteStart = ((now / 60000) + 1) * 60000; // 60000ms
        long initialDelay = nextMinuteStart - now;
        vertx.setTimer(initialDelay, id -> {
            print();
            vertx.setPeriodic(period, pid -> {
                print();
            });
        });
    }

    private static void print() {
        Runtime rt = Runtime.getRuntime();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;
        long max = rt.maxMemory();
        LogUtils.writeLog(
                EmptyRoutingContext.init(),
                LogUtils.Level.TRACE,
                "Memory monitoring:\n" +
                        "    Used Memory (MB): {}\n" +
                        "    Total Allocated (MB): {}\n" +
                        "    Free Memory (MB): {}\n" +
                        "    Max Memory (MB): {}",
                used / (1024 * 1024),
                total / (1024 * 1024),
                free / (1024 * 1024),
                max / (1024 * 1024)
        );
    }

}
