package vn.io.lcx.common.thread;

import vn.io.lcx.common.utils.LogUtils;

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
 * fire-and-forget task execution with CountDownLatch coordination and asynchronous
 * execution via CompletableFuture.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Configurable thread pool size (min/max threads)</li>
 *   <li>Multiple rejection policies (ABORT, DISCARD, CALLER_RUNS, DISCARD_OLDEST)</li>
 *   <li>Timeout support for task execution</li>
 *   <li>Virtual Thread support (JDK 21+) with automatic fallback</li>
 *   <li>AutoCloseable for proper resource cleanup</li>
 *   <li>Comprehensive logging</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Create executor with custom configuration
 * try (SimpleExecutor<String> executor = SimpleExecutor.init(
 *         2,  // min threads
 *         10, // max threads
 *         RejectMode.ABORT_POLICY,
 *         30, // timeout
 *         TimeUnit.SECONDS
 * )) {
 *     // Add tasks
 *     executor.addNewTask(() -> "Task 1 result");
 *     executor.addNewTask(() -> "Task 2 result");
 *
 *     // Execute tasks
 *     executor.executeTasksWithCountDownLatch();
 * }
 * }</pre>
 *
 * @param <T> the type of result returned by the tasks
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
    private final int minThread;

    /**
     * Maximum number of threads in the pool
     */
    private final int maxThread;

    /**
     * Timeout duration for task execution
     */
    private final long timeout;

    /**
     * Time unit for timeout
     */
    private final TimeUnit unit;

    /**
     * The underlying executor service
     */
    private ExecutorService executorService;

    /**
     * Flag to indicate whether to use Virtual Threads (JDK 21+)
     */
    private final boolean useVirtualThread;

    public SimpleExecutor(List<Callable<T>> taskList,
                          RejectedExecutionHandler rejectedExecutionHandler,
                          int minThread,
                          int maxThread,
                          long timeout,
                          TimeUnit unit,
                          boolean useVirtualThread) {
        this.taskList = taskList;
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.minThread = minThread;
        this.maxThread = maxThread;
        this.timeout = timeout;
        this.unit = unit;
        this.executorService = null;
        this.useVirtualThread = useVirtualThread;
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
                false
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
        return new SimpleExecutor<>(
                new ArrayList<>(),
                SimpleExecutor.getRejectHandlerClass(rejectMode),
                0,
                0,
                timeout,
                unit,
                false
        );
    }

    /**
     * Creates a new SimpleExecutor configured to use Virtual Threads.
     *
     * <p>Virtual Threads (Project Loom) are lightweight threads introduced in JDK 21.
     * When running on JDK 21+, this executor will use Virtual Threads for task execution.
     * When running on earlier JDK versions, it will automatically fallback to a cached
     * thread pool.</p>
     *
     * <p>Virtual Threads are ideal for I/O-bound workloads with high concurrency,
     * as they have minimal overhead compared to platform threads.</p>
     *
     * @param timeout the timeout duration for task execution
     * @param unit    the time unit for the timeout
     * @param <T>     the type of result returned by tasks
     * @return a new SimpleExecutor instance configured for Virtual Threads
     * @throws NullPointerException if unit is null
     */
    public static <T> SimpleExecutor<T> initWithVirtualThread(final long timeout,
                                                               final TimeUnit unit) {
        return new SimpleExecutor<>(
                new ArrayList<>(),
                new ThreadPoolExecutor.AbortPolicy(), // Default policy, not used for virtual threads
                0,
                0,
                timeout,
                unit,
                true
        );
    }

    /**
     * Creates a new SimpleExecutor with full configuration and Virtual Thread support.
     *
     * <p>When {@code useVirtualThread} is true and running on JDK 21+, this executor
     * will use Virtual Threads. The minThread, maxThread, and rejectedExecutionHandler
     * parameters are ignored when using Virtual Threads.</p>
     *
     * @param minNumberOfThreads the minimum number of threads (ignored for Virtual Threads)
     * @param maxNumberOfThreads the maximum number of threads (ignored for Virtual Threads)
     * @param rejectMode         the rejection policy (ignored for Virtual Threads)
     * @param timeout            the timeout duration for task execution
     * @param unit               the time unit for the timeout
     * @param useVirtualThread   whether to use Virtual Threads (requires JDK 21+)
     * @param <T>                the type of result returned by tasks
     * @return a new SimpleExecutor instance
     */
    public static <T> SimpleExecutor<T> init(int minNumberOfThreads,
                                             int maxNumberOfThreads,
                                             RejectMode rejectMode,
                                             final long timeout,
                                             final TimeUnit unit,
                                             boolean useVirtualThread) {
        return new SimpleExecutor<>(
                new ArrayList<>(),
                SimpleExecutor.getRejectHandlerClass(rejectMode),
                minNumberOfThreads,
                maxNumberOfThreads,
                timeout,
                unit,
                useVirtualThread
        );
    }

    /**
     * Converts a RejectMode enum to the corresponding RejectedExecutionHandler.
     *
     * @param rejectMode the rejection mode to convert
     * @return the corresponding RejectedExecutionHandler implementation
     * @throws IllegalArgumentException if rejectMode is null
     */
    public static RejectedExecutionHandler getRejectHandlerClass(RejectMode rejectMode) {
        return switch (rejectMode) {
            case ABORT_POLICY -> new ThreadPoolExecutor.AbortPolicy();
            case DISCARD_OLDEST_POLICY -> new ThreadPoolExecutor.DiscardOldestPolicy();
            case CALLER_RUNS_POLICY -> new ThreadPoolExecutor.CallerRunsPolicy();
            case DISCARD_POLICY -> new ThreadPoolExecutor.DiscardPolicy();
        };
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

    public int getMaxThread() {
        return maxThread;
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Checks if this executor is configured to use Virtual Threads.
     *
     * @return true if Virtual Threads should be used, false otherwise
     */
    public boolean isUseVirtualThread() {
        return useVirtualThread;
    }

    /**
     * Adds a single task to the executor's task list.
     *
     * <p>The task will be executed when {@link #executeTasksWithCountDownLatch()} is called.</p>
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
     * <p>All tasks will be executed when {@link #executeTasksWithCountDownLatch()} is called.</p>
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
    private ExecutorService createExecutorService() {
        if (executorService != null && !executorService.isTerminated()) {
            return executorService;
        }
        final ExecutorService service;

        // Try to use Virtual Threads if requested and supported
        if (useVirtualThread) {
            if (VirtualThreadSupport.isVirtualThreadSupported()) {
                ExecutorService virtualExecutor = VirtualThreadSupport.newVirtualThreadPerTaskExecutor("lcx-virtual-worker");
                if (virtualExecutor != null) {
                    LogUtils.writeLog(
                            SimpleExecutor.class,
                            LogUtils.Level.INFO,
                            "Using Virtual Thread executor (JDK {})",
                            VirtualThreadSupport.getJdkMajorVersion()
                    );
                    executorService = virtualExecutor;
                    return virtualExecutor;
                }
            } else {
                LogUtils.writeLog(
                        SimpleExecutor.class,
                        LogUtils.Level.WARN,
                        "Virtual Threads requested but not supported (JDK {} < 21). Falling back to platform threads.",
                        VirtualThreadSupport.getJdkMajorVersion()
                );
            }
        }

        // Fallback to platform threads
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
                    new LinkedBlockingQueue<>(Math.max(this.taskList.size(), this.maxThread)),
                    new LcxThreadFactory.MyThreadFactory("lcx-worker"),
                    this.rejectedExecutionHandler
            );
        }
        executorService = service;
        return service;
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
                SimpleExecutor.class,
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
                            SimpleExecutor.class,
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
        try {
            var finishedInTime = latch.await(this.timeout, this.unit);
            if (finishedInTime) {
                LogUtils.writeLog(this.getClass(), LogUtils.Level.INFO, "All tasks finished in time");
            } else {
                LogUtils.writeLog(this.getClass(), LogUtils.Level.WARN, "Some task has not been done");
            }
        } catch (InterruptedException e) {
            LogUtils.writeLog(this.getClass(), e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for tasks", e);
        } finally {
            taskList.clear();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    LogUtils.writeLog(this.getClass(), LogUtils.Level.WARN, "Executor did not terminate in time, forcing shutdown...");
                    executor.shutdownNow();

                    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                        LogUtils.writeLog(this.getClass(), LogUtils.Level.ERROR, "Executor did not terminate even after forced shutdown");
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
        // Create a separate executor to avoid shutting down the shared executorService
        final ExecutorService localExecutor = Executors.newCachedThreadPool(
                new LcxThreadFactory.MyThreadFactory("lcx-async-worker")
        );
        CompletableFuture<Void> currentFuture = CompletableFuture.completedFuture(null);
        for (Runnable r : runnable) {
            currentFuture = currentFuture.thenRunAsync(r, localExecutor);
        }
        return currentFuture.whenComplete((result, error) -> localExecutor.shutdown());
    }

    /**
     * Shuts down the executor service gracefully with two-phase termination.
     *
     * <p>This method initiates a graceful shutdown, waits up to 30 seconds for
     * running tasks to complete, then forces shutdown if necessary.</p>
     */
    @Override
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    LogUtils.writeLog(this.getClass(), LogUtils.Level.WARN, "Executor did not terminate in time, forcing shutdown...");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
