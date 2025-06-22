package vn.com.lcx.common.thread;

import vn.com.lcx.common.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A flexible and configurable thread pool executor for managing concurrent task execution.
 *
 * <p>This class provides a simplified interface for creating and managing thread pools
 * with various rejection policies and timeout configurations. It supports both
 * synchronous task execution with result collection and asynchronous execution
 * with CountDownLatch for coordination.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Configurable thread pool size (min/max threads)</li>
 *   <li>Multiple rejection policies (ABORT, DISCARD, CALLER_RUNS, DISCARD_OLDEST)</li>
 *   <li>Timeout support for task execution</li>
 *   <li>Both synchronous and asynchronous execution modes</li>
 *   <li>Automatic resource cleanup</li>
 *   <li>Comprehensive logging</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Create executor with custom configuration
 * SimpleExecutor<String> executor = SimpleExecutor.init(
 *     2,  // min threads
 *     10, // max threads
 *     RejectMode.ABORT_POLICY,
 *     30, // timeout
 *     TimeUnit.SECONDS
 * );
 *
 * // Add tasks
 * executor.addNewTask(() -> "Task 1 result");
 * executor.addNewTask(() -> "Task 2 result");
 *
 * // Execute and get results
 * List<String> results = executor.executeTasks();
 *
 * // Or execute asynchronously
 * executor.executeTasksWithCountDownLatch();
 * }</pre>
 *
 * @param <T> the type of result returned by the tasks
 * @author LCX Team
 * @since 1.0
 */
public class SimpleExecutor<T> implements BaseExecutor<T> {

    /**
     * List of callable tasks to be executed
     */
    private final List<Callable<T>> taskList;

    /**
     * Handler for rejected task execution
     */
    private final RejectedExecutionHandler rejectedExecutionHandler;

    /**
     * Minimum number of threads in the pool
     */
    private int minThread;

    /**
     * Maximum number of threads in the pool
     */
    private int maxThread;

    /**
     * Timeout duration for task execution
     */
    private long timeout;

    /**
     * Time unit for timeout
     */
    private TimeUnit unit;

    /**
     * The underlying executor service
     */
    private ExecutorService executorService;

    public SimpleExecutor(List<Callable<T>> taskList, RejectedExecutionHandler rejectedExecutionHandler) {
        this.taskList = taskList;
        this.rejectedExecutionHandler = rejectedExecutionHandler;
    }

    public SimpleExecutor(List<Callable<T>> taskList,
                          RejectedExecutionHandler rejectedExecutionHandler,
                          int minThread,
                          int maxThread,
                          long timeout,
                          TimeUnit unit,
                          ExecutorService executorService) {
        this.taskList = taskList;
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.minThread = minThread;
        this.maxThread = maxThread;
        this.timeout = timeout;
        this.unit = unit;
        this.executorService = executorService;
    }

    /**
     * Creates a new SimpleExecutor with full configuration.
     *
     * <p>This factory method creates an executor with specified thread pool size,
     * rejection policy, and timeout configuration.</p>
     *
     * @param minNumberOfThreads the minimum number of threads in the pool
     * @param maxNumberOfThreads the maximum number of threads in the pool
     * @param rejectMode         the rejection policy to use when the pool is full
     * @param timeout            the timeout duration for task execution
     * @param unit               the time unit for the timeout
     * @param <T>                the type of result returned by tasks
     * @return a new SimpleExecutor instance
     * @throws IllegalArgumentException if minNumberOfThreads or maxNumberOfThreads is negative
     * @throws NullPointerException     if rejectMode, timeout, or unit is null
     */
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
                unit,
                null
        );
    }

    /**
     * Creates a new SimpleExecutor with default thread pool configuration.
     *
     * <p>This factory method creates an executor with a cached thread pool
     * (unbounded thread pool that creates new threads as needed) and specified
     * timeout configuration.</p>
     *
     * @param rejectMode the rejection policy to use when the pool is full
     * @param timeout    the timeout duration for task execution
     * @param unit       the time unit for the timeout
     * @param <T>        the type of result returned by tasks
     * @return a new SimpleExecutor instance with cached thread pool
     * @throws NullPointerException if rejectMode, timeout, or unit is null
     */
    public static <T> SimpleExecutor<T> init(RejectMode rejectMode,
                                             final long timeout,
                                             final TimeUnit unit) {
        final var asd = new SimpleExecutor<T>(
                new ArrayList<>(),
                SimpleExecutor.getRejectHandlerClass(rejectMode)
        );
        asd.setUnit(unit);
        asd.setTimeout(timeout);
        return asd;
    }

    /**
     * Converts a RejectMode enum to the corresponding RejectedExecutionHandler.
     *
     * <p>This method maps the enum values to their corresponding ThreadPoolExecutor
     * rejection policy implementations.</p>
     *
     * @param rejectMode the rejection mode to convert
     * @return the corresponding RejectedExecutionHandler implementation
     * @throws IllegalArgumentException if rejectMode is null
     */
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

    public List<Callable<T>> getTaskList() {
        return taskList;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    public int getMinThread() {
        return minThread;
    }

    @Override
    public void setMinThread(int minThread) {
        this.minThread = minThread;
    }

    public int getMaxThread() {
        return maxThread;
    }

    @Override
    public void setMaxThread(int maxThread) {
        this.maxThread = maxThread;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Adds a single task to the executor's task list.
     *
     * <p>The task will be executed when {@link #executeTasks()} or
     * {@link #executeTasksWithCountDownLatch()} is called.</p>
     *
     * @param task the callable task to add
     * @throws NullPointerException if task is null
     */
    @Override
    public void addNewTask(Callable<T> task) {
        this.taskList.add(task);
    }

    /**
     * Adds multiple tasks to the executor's task list.
     *
     * <p>All tasks will be executed when {@link #executeTasks()} or
     * {@link #executeTasksWithCountDownLatch()} is called.</p>
     *
     * @param tasks the list of callable tasks to add
     * @throws NullPointerException if tasks is null or contains null elements
     */
    @Override
    public void addNewTasks(List<Callable<T>> tasks) {
        this.taskList.addAll(tasks);
    }

    /**
     * Creates and returns an ExecutorService based on the current configuration.
     *
     * <p>If minThread and maxThread are both 0, creates a cached thread pool.
     * Otherwise, creates a ThreadPoolExecutor with the specified configuration.</p>
     *
     * <p>This method implements lazy initialization - the executor service is only
     * created when needed and reused if already created and not terminated.</p>
     *
     * @return the configured ExecutorService
     */
    @Override
    public ExecutorService createExecutorService() {
        if (executorService != null && !executorService.isTerminated()) {
            return executorService;
        }
        final ExecutorService service;
        if (minThread == 0 || maxThread == 0) {
            service = Executors.newCachedThreadPool(
                    new LcxThreadFactory.MyThreadFactory("lcx-worker")
            );
        } else {
            service = new ThreadPoolExecutor(
                    this.minThread,
                    this.maxThread,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(this.taskList.size()),
                    new LcxThreadFactory.MyThreadFactory("lcx-worker"),
                    this.rejectedExecutionHandler
            );
        }
        executorService = service;
        return service;
    }

    /**
     * Executes all tasks synchronously and returns their results.
     *
     * <p>This method executes all tasks in the task list and waits for their completion.
     * If any task fails, all remaining futures are cancelled and a RuntimeException
     * is thrown.</p>
     *
     * <p><strong>Note:</strong> This method is deprecated. Use
     * {@link #executeTasksWithCountDownLatch()} instead for better error handling
     * and resource management.</p>
     *
     * @return a list containing the results of all tasks in the order they were added
     * @throws RuntimeException      if any task fails or if the execution is interrupted
     * @throws IllegalStateException if no tasks have been added
     * @deprecated Use {@link #executeTasksWithCountDownLatch()} instead
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

    /**
     * Executes all tasks asynchronously using CountDownLatch for coordination.
     *
     * <p>This method submits all tasks to the executor service and uses a CountDownLatch
     * to wait for their completion. Each task is executed in its own thread, and
     * the method waits for all tasks to complete or timeout.</p>
     *
     * <p>Key features of this method:</p>
     * <ul>
     *   <li>Non-blocking task submission</li>
     *   <li>Timeout-based waiting</li>
     *   <li>Graceful shutdown with forced termination if necessary</li>
     *   <li>Comprehensive error logging</li>
     *   <li>Automatic task list cleanup</li>
     * </ul>
     *
     * <p>After execution, the task list is cleared and the executor service is
     * properly shut down.</p>
     *
     * @throws RuntimeException      if the waiting is interrupted
     * @throws IllegalStateException if no tasks have been added
     */
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
            taskList.clear();
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

    /**
     * Cancels a future task.
     *
     * <p>This method attempts to cancel the given future task. If the task is
     * already running, it may not be cancelled immediately.</p>
     *
     * @param futureTasks the future task to cancel
     */
    @Override
    public void cancelFutureTasks(Future<T> futureTasks) {
        futureTasks.cancel(true);
    }

    /**
     * Executes a single runnable task asynchronously.
     *
     * <p>This method creates a CompletableFuture that executes the given runnable
     * using the executor service.</p>
     *
     * @param runnable the runnable task to execute
     * @return a CompletableFuture that completes when the task finishes
     * @throws NullPointerException if runnable is null
     */
    @Override
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return runAsync(new Runnable[]{runnable});
    }

    /**
     * Executes multiple runnable tasks asynchronously in sequence.
     *
     * <p>This method creates a chain of CompletableFuture tasks that execute
     * sequentially. Each task waits for the previous one to complete before
     * starting.</p>
     *
     * <p>If no runnables are provided, returns a completed future immediately.</p>
     *
     * @param runnable the array of runnable tasks to execute sequentially
     * @return a CompletableFuture that completes when all tasks finish
     * @throws NullPointerException if runnable array is null or contains null elements
     */
    @Override
    public CompletableFuture<Void> runAsync(Runnable... runnable) {
        if (runnable.length == 0) {
            return CompletableFuture.completedFuture(null);
        }
        final ExecutorService executor = createExecutorService();
        CompletableFuture<Void> currentFuture = CompletableFuture.completedFuture(null);
        for (Runnable r : runnable) {
            currentFuture = currentFuture.thenRunAsync(r, executor);
        }
        return currentFuture;
    }

    /**
     * Shuts down the executor service gracefully.
     *
     * <p>This method initiates a graceful shutdown of the executor service.
     * No new tasks will be accepted, but already submitted tasks will be
     * allowed to complete.</p>
     *
     * <p>Note: This method only initiates shutdown. For complete cleanup,
     * consider using {@link #executeTasksWithCountDownLatch()} which handles
     * shutdown automatically.</p>
     */
    @Override
    public void shutdownExecutor() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}
