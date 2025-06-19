package vn.com.lcx.common.thread;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class SimpleExecutorTest {

    @Mock
    private ExecutorService mockExecutorService;

    @Mock
    private RejectedExecutionHandler mockRejectedExecutionHandler;

    @Mock
    private Callable<String> mockTask;

    @Mock
    private Future<String> mockFuture;

    private SimpleExecutor<String> executor;
    private List<Callable<String>> taskList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskList = new ArrayList<>();
        executor = new SimpleExecutor<>(taskList, mockRejectedExecutionHandler, 2, 4, 1000, TimeUnit.MILLISECONDS, null);
    }

    @Test
    void testInit_WithMinMaxThreads() {
        SimpleExecutor<String> executor = SimpleExecutor.init(2, 4, RejectMode.ABORT_POLICY, 1000, TimeUnit.MILLISECONDS);
        
        assertEquals(2, executor.getMinThread());
        assertEquals(4, executor.getMaxThread());
        assertEquals(1000, executor.getTimeout());
        assertEquals(TimeUnit.MILLISECONDS, executor.getUnit());
        assertNotNull(executor.getRejectedExecutionHandler());
        assertTrue(executor.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy);
    }

    @Test
    void testInit_WithoutMinMaxThreads() {
        SimpleExecutor<String> executor = SimpleExecutor.init(RejectMode.CALLER_RUNS_POLICY, 2000, TimeUnit.SECONDS);
        
        assertEquals(0, executor.getMinThread());
        assertEquals(0, executor.getMaxThread());
        assertEquals(2000, executor.getTimeout());
        assertEquals(TimeUnit.SECONDS, executor.getUnit());
        assertNotNull(executor.getRejectedExecutionHandler());
        assertTrue(executor.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.CallerRunsPolicy);
    }

    @ParameterizedTest
    @EnumSource(RejectMode.class)
    void testGetRejectHandlerClass(RejectMode rejectMode) {
        RejectedExecutionHandler handler = SimpleExecutor.getRejectHandlerClass(rejectMode);
        
        assertNotNull(handler);
        
        switch (rejectMode) {
            case ABORT_POLICY:
                assertTrue(handler instanceof ThreadPoolExecutor.AbortPolicy);
                break;
            case DISCARD_OLDEST_POLICY:
                assertTrue(handler instanceof ThreadPoolExecutor.DiscardOldestPolicy);
                break;
            case CALLER_RUNS_POLICY:
                assertTrue(handler instanceof ThreadPoolExecutor.CallerRunsPolicy);
                break;
            case DISCARD_POLICY:
                assertTrue(handler instanceof ThreadPoolExecutor.DiscardPolicy);
                break;
        }
    }

    @Test
    void testAddNewTask() {
        Callable<String> task = () -> "test";
        executor.addNewTask(task);
        
        assertEquals(1, taskList.size());
        assertSame(task, taskList.get(0));
    }

    @Test
    void testAddNewTasks() {
        List<Callable<String>> tasks = Arrays.asList(
            () -> "task1",
            () -> "task2"
        );
        
        executor.addNewTasks(tasks);
        
        assertEquals(2, taskList.size());
        assertSame(tasks.get(0), taskList.get(0));
        assertSame(tasks.get(1), taskList.get(1));
    }

    @Test
    void testCreateExecutorService_WithMinMaxThreads() {
        executor.addNewTask(() -> "test");
        ExecutorService service = executor.createExecutorService();
        
        assertNotNull(service);
        assertSame(service, executor.getExecutorService());
    }

    @Test
    void testCreateExecutorService_WithoutMinMaxThreads() {
        SimpleExecutor<String> executor = new SimpleExecutor<>(
                taskList, mockRejectedExecutionHandler, 0, 0, 1000, TimeUnit.MILLISECONDS, null);
        
        ExecutorService service = executor.createExecutorService();
        
        assertNotNull(service);
        assertSame(service, executor.getExecutorService());
    }

    @Test
    void testCreateExecutorService_ReuseExistingService() {
        executor.setExecutorService(mockExecutorService);
        when(mockExecutorService.isTerminated()).thenReturn(false);
        
        ExecutorService service = executor.createExecutorService();
        
        assertSame(mockExecutorService, service);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testExecuteTasks() throws Exception {
        // Setup
        Callable<String> task1 = () -> "result1";
        Callable<String> task2 = () -> "result2";
        taskList.add(task1);
        taskList.add(task2);
        
        executor.setExecutorService(mockExecutorService);
        
        List<Future<String>> futures = new ArrayList<>();
        Future<String> future1 = mock(Future.class);
        Future<String> future2 = mock(Future.class);
        futures.add(future1);
        futures.add(future2);
        
        when(mockExecutorService.invokeAll(taskList, 1000, TimeUnit.MILLISECONDS)).thenReturn(futures);
        when(future1.get()).thenReturn("result1");
        when(future2.get()).thenReturn("result2");
        
        // Execute
        List<String> results = executor.executeTasks();
        
        // Verify
        assertEquals(2, results.size());
        assertEquals("result1", results.get(0));
        assertEquals("result2", results.get(1));
        verify(mockExecutorService).invokeAll(taskList, 1000, TimeUnit.MILLISECONDS);
        verify(mockExecutorService).shutdown();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testExecuteTasks_WithoutTimeout() throws Exception {
        // Setup
        executor.setTimeout(0);
        
        Callable<String> task = () -> "result";
        taskList.add(task);
        
        executor.setExecutorService(mockExecutorService);
        
        List<Future<String>> futures = new ArrayList<>();
        Future<String> future = mock(Future.class);
        futures.add(future);
        
        when(mockExecutorService.invokeAll(taskList)).thenReturn(futures);
        when(future.get()).thenReturn("result");
        
        // Execute
        List<String> results = executor.executeTasks();
        
        // Verify
        assertEquals(1, results.size());
        assertEquals("result", results.get(0));
        verify(mockExecutorService).invokeAll(taskList);
        verify(mockExecutorService).shutdown();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testExecuteTasks_WithException() throws Exception {
        // Setup
        Callable<String> task = () -> "result";
        taskList.add(task);
        
        executor.setExecutorService(mockExecutorService);
        
        List<Future<String>> futures = new ArrayList<>();
        Future<String> future = mock(Future.class);
        futures.add(future);
        
        when(mockExecutorService.invokeAll(taskList, 1000, TimeUnit.MILLISECONDS)).thenReturn(futures);
        when(future.get()).thenThrow(new RuntimeException("Task failed"));
        
        // Execute
        List<String> results = executor.executeTasks();
        
        // Verify
        assertEquals(0, results.size());
        verify(mockExecutorService).invokeAll(taskList, 1000, TimeUnit.MILLISECONDS);
        verify(future).cancel(true);
        verify(mockExecutorService).shutdown();
    }

    @Test
    void testExecuteTasksWithCountDownLatch() throws Exception {
        // Setup
        AtomicInteger counter = new AtomicInteger(0);
        
        Callable<String> task1 = () -> {
            counter.incrementAndGet();
            return "result1";
        };
        
        Callable<String> task2 = () -> {
            counter.incrementAndGet();
            return "result2";
        };
        
        taskList.add(task1);
        taskList.add(task2);
        
        // Use real executor service for this test
        executor.setMinThread(1);
        executor.setMaxThread(2);
        
        // Execute
        executor.executeTasksWithCountDownLatch();
        
        // Wait a bit to ensure tasks complete
        TimeUnit.MILLISECONDS.sleep(500);
        
        // Verify
        assertEquals(2, counter.get());
        assertTrue(taskList.isEmpty()); // Task list should be cleared
    }

    @Test
    void testCancelFutureTasks() {
        executor.cancelFutureTasks(mockFuture);
        verify(mockFuture).cancel(true);
    }

    @Test
    void testRunAsync_SingleRunnable() {
        // Setup
        executor.setExecutorService(mockExecutorService);
        Runnable runnable = mock(Runnable.class);
        
        // Execute
        CompletableFuture<Void> future = executor.runAsync(runnable);
        
        // Verify
        assertNotNull(future);
    }

    @Test
    void testRunAsync_MultipleRunnables() {
        // Setup
        executor.setExecutorService(mockExecutorService);
        Runnable runnable1 = mock(Runnable.class);
        Runnable runnable2 = mock(Runnable.class);
        
        // Execute
        CompletableFuture<Void> future = executor.runAsync(runnable1, runnable2);
        
        // Verify
        assertNotNull(future);
    }

    @Test
    void testRunAsync_EmptyRunnables() {
        // Setup
        executor.setExecutorService(mockExecutorService);
        
        // Execute
        CompletableFuture<Void> future = executor.runAsync();
        
        // Verify
        assertNotNull(future);
        assertTrue(future.isDone());
    }

    @Test
    void testShutdownExecutor() {
        // Setup
        executor.setExecutorService(mockExecutorService);
        when(mockExecutorService.isShutdown()).thenReturn(false);
        
        // Execute
        executor.shutdownExecutor();
        
        // Verify
        verify(mockExecutorService).shutdown();
    }

    @Test
    void testShutdownExecutor_AlreadyShutdown() {
        // Setup
        executor.setExecutorService(mockExecutorService);
        when(mockExecutorService.isShutdown()).thenReturn(true);
        
        // Execute
        executor.shutdownExecutor();
        
        // Verify
        verify(mockExecutorService, never()).shutdown();
    }

    @Test
    @Timeout(5) // Ensure this test doesn't hang
    void testExecuteTasksWithCountDownLatch_WithFailingTask() throws Exception {
        // Setup
        Callable<String> task1 = () -> "result1";
        Callable<String> task2 = () -> { throw new RuntimeException("Task failed"); };
        
        taskList.add(task1);
        taskList.add(task2);
        
        // Use real executor service for this test
        executor.setMinThread(1);
        executor.setMaxThread(2);
        
        // Execute
        executor.executeTasksWithCountDownLatch();
        
        // Wait a bit to ensure tasks complete
        TimeUnit.MILLISECONDS.sleep(500);
        
        // Verify task list is cleared even if a task fails
        assertTrue(taskList.isEmpty());
    }
}
