package vn.com.lcx.common.thread;

import lombok.NoArgsConstructor;
import lombok.val;
import vn.com.lcx.common.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
public class MyExecutor<T> {

    @SuppressWarnings("UnusedReturnValue")
    public List<T> submitTaskAndWait(final List<Callable<T>> callables,
                                     final long timeout,
                                     final TimeUnit unit,
                                     final int numberOfCorePoolSize,
                                     boolean skipIfTaskFailed,
                                     boolean... logExecutionInfo) {
        val needToLogExecutionInfo = logExecutionInfo.length > 0 && logExecutionInfo[0];
        if (needToLogExecutionInfo) {
            LogUtils.writeLog(
                    LogUtils.Level.INFO,
                    "Execution info:\n" +
                            "    - Task list: {}\n" +
                            "    - Number Of Core Pool Size: {}\n" +
                            "    - Timeout: {}\n" +
                            "    - Time unit: {}",
                    callables.size(),
                    numberOfCorePoolSize,
                    timeout,
                    unit.toString()
            );
        }
        List<T> results = new ArrayList<>(callables.size());
        ExecutorService executor = Executors.newFixedThreadPool(numberOfCorePoolSize);
        try {
            List<Future<T>> futures;
            if (timeout <= 0 || unit == null) {
                futures = executor.invokeAll(callables);
            } else {
                futures = executor.invokeAll(callables, timeout, unit);
            }
            while (!futures.isEmpty()) {
                val future = futures.remove(0);
                try {
                    results.add(future.get());
                } catch (Throwable e) {
                    LogUtils.writeLog(e.getMessage(), e);
                    future.cancel(true);
                    if (skipIfTaskFailed) {
                        if (needToLogExecutionInfo) {
                            LogUtils.writeLog("task failed", e);
                        }
                    } else {
                        futures.forEach(futureTask -> futureTask.cancel(true));
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
        return results;
    }

}
