package vn.com.lcx.common.thread;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class SimpleExecutor<T> implements BaseExecutor<T> {

    private final List<Callable<T>> taskList;
    private final RejectedExecutionHandler rejectedExecutionHandler;
    private int minThread;
    private int maxThread;
    private long timeout;
    private TimeUnit unit;

    public static <T> SimpleExecutor<T> init(int minNumberOfThreads,
                                             int maxNumberOfThreads,
                                             RejectMode rejectMode,
                                             final long timeout,
                                             final TimeUnit unit) {
        return new SimpleExecutor<>(
                new ArrayList<>(),
                SimpleExecutor.getRejectHandlerClass(rejectMode),
                minNumberOfThreads,
                maxNumberOfThreads,
                timeout,
                unit
        );
    }

    public static <T> SimpleExecutor<T> init(RejectMode rejectMode,
                                             final long timeout,
                                             final TimeUnit unit,
                                             final boolean isUsingVirtualThread) {
        final var asd = new SimpleExecutor<T>(
                new ArrayList<>(),
                SimpleExecutor.getRejectHandlerClass(rejectMode)
        );
        asd.setUnit(unit);
        asd.setTimeout(timeout);
        return asd;
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static RejectedExecutionHandler getRejectHandlerClass(RejectMode rejectMode) {
        RejectedExecutionHandler rejectedExecutionHandler;
        switch (rejectMode) {
            case ABORT_POLICY:
                rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
                break;
            case DISCARD_OLDEST_POLICY:
                rejectedExecutionHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case CALLER_RUNS_POLICY:
                rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            case DISCARD_POLICY:
                rejectedExecutionHandler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            default:
                rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }
        return rejectedExecutionHandler;
    }

    @Override
    public void addNewTask(Callable<T> task) {
        this.taskList.add(task);
    }

    @Override
    public void addNewTasks(List<Callable<T>> tasks) {
        this.taskList.addAll(tasks);
    }

    @SuppressWarnings("Convert2Diamond")
    @Override
    public ExecutorService createExecutorService() {
        if (minThread == 0 || maxThread == 0) {
            return Executors.newCachedThreadPool(
                    new LcxThreadFactory.MyThreadFactory("lcx-worker")
            );
        }
        return new ThreadPoolExecutor(
                this.minThread,
                this.maxThread,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(this.taskList.size()),
                new LcxThreadFactory.MyThreadFactory("lcx-worker"),
                this.rejectedExecutionHandler
        );
    }

    /**
     * @return List of return value of tasks.
     * @deprecated Use {@link SimpleExecutor#executeTasksWithCountDownLatch()}.
     */
    @Override
    @Deprecated
    public List<T> executeTasks() {
        LogUtils.writeLog(
                LogUtils.Level.INFO,
                "Execution info:\n" +
                        "    - Task list: {}\n" +
                        "    - Rejected execution handler: {}\n" +
                        "    - Min number of thread(s): {}\n" +
                        "    - Max number of thread(s): {}\n" +
                        "    - Timeout: {}\n" +
                        "    - Time unit: {}",
                this.taskList.size(),
                this.rejectedExecutionHandler.getClass().getSimpleName(),
                this.minThread,
                this.maxThread,
                this.timeout,
                this.unit.toString()
        );
        List<T> result = new ArrayList<>(this.taskList.size());
        ExecutorService executor = this.createExecutorService();
        try {
            List<Future<T>> futures;
            if (this.timeout <= 0 || this.unit == null) {
                futures = executor.invokeAll(this.taskList);
            } else {
                futures = executor.invokeAll(this.taskList, this.timeout, this.unit);
            }

            while (!futures.isEmpty()) {
                final var future = futures.remove(0);
                try {
                    result.add(future.get());
                } catch (Throwable e) {
                    LogUtils.writeLog(e.getMessage(), e);
                    future.cancel(true);
                    futures.forEach(this::cancelFutureTasks);
                    // throw new RuntimeException("Task failed due to " + e, e);
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            // Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
        return result;
    }

    @Override
    public void executeTasksWithCountDownLatch() {
        LogUtils.writeLog(
                LogUtils.Level.INFO,
                "Execution info:\n" +
                        "    - Task list: {}\n" +
                        "    - Rejected execution handler: {}\n" +
                        "    - Min number of thread(s): {}\n" +
                        "    - Max number of thread(s): {}\n" +
                        "    - Timeout: {}\n" +
                        "    - Time unit: {}",
                this.taskList.size(),
                this.rejectedExecutionHandler.getClass().getSimpleName(),
                this.minThread,
                this.maxThread,
                this.timeout,
                this.unit.toString()
        );
        ExecutorService executor = this.createExecutorService();
        final CountDownLatch latch = new CountDownLatch(this.taskList.size());
        for (int i = 0; i < this.taskList.size(); i++) {
            final int taskIndex = i;
            final Callable<T> tCallable = this.taskList.get(i);

            executor.submit(() -> {
                try {
                    tCallable.call();
                } catch (Exception e) {
                    LogUtils.writeLog(
                            LogUtils.Level.ERROR,
                            "Task [{}] failed with error: {}",
                            taskIndex,
                            e.getMessage(),
                            e
                    );
                } finally {
                    latch.countDown();
                }
            });
        }
        executor.shutdown();
        var finishedInTime = false;
        try {
            finishedInTime = latch.await(this.timeout, this.unit);
            if (finishedInTime) {
                LogUtils.writeLog(LogUtils.Level.INFO, "All tasks finished in time");
            } else {
                LogUtils.writeLog(LogUtils.Level.WARN, "Some task has not been done");
            }
        } catch (InterruptedException e) {
            LogUtils.writeLog(e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for tasks", e);
        } finally {
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    LogUtils.writeLog(LogUtils.Level.WARN, "Executor did not terminate in time, forcing shutdown...");
                    executor.shutdownNow();

                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        LogUtils.writeLog(LogUtils.Level.ERROR, "Executor did not terminate even after forced shutdown");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void cancelFutureTasks(Future<T> futureTasks) {
        futureTasks.cancel(true);
    }
}
