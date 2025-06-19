package vn.com.lcx.common.database.pool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.com.lcx.common.database.DatabaseProperty;
import vn.com.lcx.common.database.pool.entry.ConnectionEntry;
import vn.com.lcx.common.database.type.DBTypeEnum;
import vn.com.lcx.common.exception.LCXDataSourceException;
import vn.com.lcx.common.thread.SimpleExecutor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LCXDataSourcePerformanceTest {

    private LCXDataSource dataSource;
    private DatabaseProperty mockProperty;
    private SimpleExecutor<Boolean> mockExecutor;
    private ConcurrentLinkedQueue<ConnectionEntry> realPool;

    @BeforeEach
    void setUp() {
        mockProperty = mock(DatabaseProperty.class);
        mockExecutor = mock(SimpleExecutor.class);
        realPool = new ConcurrentLinkedQueue<>();

        // Setup default mock behavior
        when(mockProperty.getConnectionString()).thenReturn("jdbc:postgresql://localhost:5432/testdb");
        when(mockProperty.getUsername()).thenReturn("testuser");
        when(mockProperty.getPassword()).thenReturn("testpass");
        when(mockProperty.getDriverClassName()).thenReturn("org.postgresql.Driver");
        when(mockProperty.getInitialPoolSize()).thenReturn(10);
        when(mockProperty.getMaxPoolSize()).thenReturn(50);
        when(mockProperty.getMaxTimeout()).thenReturn(30);
        when(mockProperty.propertiesIsAllSet()).thenReturn(true);

        dataSource = new LCXDataSource(
                "test-pool",
                "org.postgresql.Driver",
                "SELECT version()",
                mockProperty,
                mockExecutor,
                DBTypeEnum.POSTGRESQL,
                30000,
                realPool
        );
    }

    @Test
    void testHighConcurrencyConnectionRequests() throws InterruptedException {
        // Arrange
        int threadCount = 100;
        int requestsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Create mock connections
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(50);
        realPool.addAll(mockEntries);

        long startTime = System.currentTimeMillis();

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        try {
                            Connection conn = dataSource.get();
                            if (conn != null) {
                                successCount.incrementAndGet();
                                // Simulate some work
                                Thread.sleep(10);
                                conn.close();
                            }
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "Test should complete within timeout");
        assertTrue(successCount.get() > 0, "Should have successful connections");
        System.out.println("Performance Test Results:");
        System.out.println("Total requests: " + (threadCount * requestsPerThread));
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());
        System.out.println("Total time: " + (endTime - startTime) + "ms");
        System.out.println("Throughput: " + (successCount.get() * 1000.0 / (endTime - startTime)) + " requests/second");
    }

    @Test
    void testConnectionPoolUnderLoad() throws InterruptedException {
        // Arrange
        int threadCount = 20;
        int requestsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger timeoutCount = new AtomicInteger(0);

        // Create limited pool size to test contention
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(5);
        realPool.addAll(mockEntries);

        long startTime = System.currentTimeMillis();

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        try {
                            Connection conn = dataSource.get();
                            if (conn != null) {
                                successCount.incrementAndGet();
                                // Simulate database work
                                Thread.sleep(50);
                                conn.close();
                            }
                        } catch (LCXDataSourceException e) {
                            if (e.getMessage().contains("busy")) {
                                timeoutCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            // Other exceptions
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "Test should complete within timeout");
        assertTrue(successCount.get() > 0, "Should have successful connections");
        System.out.println("Load Test Results:");
        System.out.println("Total requests: " + (threadCount * requestsPerThread));
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Timeout requests: " + timeoutCount.get());
        System.out.println("Total time: " + (endTime - startTime) + "ms");
    }

    @Test
    void testConnectionPoolMemoryUsage() throws SQLException, ClassNotFoundException {
        // Arrange
        int connectionCount = 1000;
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(connectionCount);
        realPool.addAll(mockEntries);

        // Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // Act - Simulate heavy usage
        for (int i = 0; i < 100; i++) {
            Connection conn = dataSource.get();
            conn.close();
        }

        // Get final memory usage
        System.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;

        // Assert
        assertTrue(memoryUsed < 10 * 1024 * 1024, "Memory usage should be reasonable (< 10MB)");
        System.out.println("Memory Usage Test:");
        System.out.println("Initial memory: " + initialMemory + " bytes");
        System.out.println("Final memory: " + finalMemory + " bytes");
        System.out.println("Memory used: " + memoryUsed + " bytes");
    }

    @Test
    void testConnectionPoolResponseTime() throws SQLException {
        // Arrange
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(10);
        realPool.addAll(mockEntries);

        int iterations = 1000;
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;

        // Act
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            Connection conn = dataSource.get();
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            totalTime += duration;
            minTime = Math.min(minTime, duration);
            maxTime = Math.max(maxTime, duration);

            conn.close();
        }

        // Calculate statistics
        double avgTime = (double) totalTime / iterations;
        double avgTimeMs = avgTime / 1_000_000; // Convert to milliseconds

        // Assert
        assertTrue(avgTimeMs < 10, "Average response time should be less than 10ms");
        assertTrue(minTime > 0, "Minimum time should be positive");
        assertTrue(maxTime > minTime, "Maximum time should be greater than minimum time");

        System.out.println("Response Time Test Results:");
        System.out.println("Iterations: " + iterations);
        System.out.println("Average time: " + String.format("%.2f", avgTimeMs) + "ms");
        System.out.println("Minimum time: " + (minTime / 1_000_000) + "ms");
        System.out.println("Maximum time: " + (maxTime / 1_000_000) + "ms");
    }

    @Test
    void testConnectionPoolScalability() throws SQLException, ClassNotFoundException {
        // Arrange
        int[] poolSizes = {1, 5, 10, 20, 50};
        int requestsPerSize = 100;

        for (int poolSize : poolSizes) {
            // Reset pool
            realPool.clear();
            List<ConnectionEntry> mockEntries = createMockConnectionEntries(poolSize);
            realPool.addAll(mockEntries);

            long startTime = System.nanoTime();

            // Act
            for (int i = 0; i < requestsPerSize; i++) {
                Connection conn = dataSource.get();
                conn.close();
            }

            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            double avgTimeMs = (double) totalTime / requestsPerSize / 1_000_000;

            System.out.println("Pool size " + poolSize + ": " + String.format("%.2f", avgTimeMs) + "ms average");

            // Assert
            assertTrue(avgTimeMs < 50, "Response time should be reasonable for pool size " + poolSize);
        }
    }

    @Test
    void testConnectionPoolStressTest() throws InterruptedException {
        // Arrange
        int threadCount = 50;
        int durationSeconds = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger totalRequests = new AtomicInteger(0);
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);

        // Create pool
        List<ConnectionEntry> mockEntries = createMockConnectionEntries(10);
        realPool.addAll(mockEntries);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    while (System.currentTimeMillis() < endTime) {
                        totalRequests.incrementAndGet();
                        try {
                            Connection conn = dataSource.get();
                            if (conn != null) {
                                successfulRequests.incrementAndGet();
                                // Simulate work
                                Thread.sleep(10);
                                conn.close();
                            }
                        } catch (Exception e) {
                            failedRequests.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        boolean completed = latch.await(durationSeconds + 5, TimeUnit.SECONDS);
        long actualEndTime = System.currentTimeMillis();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Assert
        assertTrue(completed, "Stress test should complete within timeout");
        assertTrue(successfulRequests.get() > 0, "Should have successful requests");
        assertTrue(totalRequests.get() > 0, "Should have total requests");

        System.out.println("Stress Test Results:");
        System.out.println("Duration: " + (actualEndTime - startTime) + "ms");
        System.out.println("Total requests: " + totalRequests.get());
        System.out.println("Successful requests: " + successfulRequests.get());
        System.out.println("Failed requests: " + failedRequests.get());
        System.out.println("Success rate: " + String.format("%.2f",
                (double) successfulRequests.get() / totalRequests.get() * 100) + "%");
        System.out.println("Throughput: " + String.format("%.2f",
                (double) successfulRequests.get() * 1000 / (actualEndTime - startTime)) + " requests/second");
    }

    private List<ConnectionEntry> createMockConnectionEntries(int count) {
        List<ConnectionEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ConnectionEntry mockEntry = mock(ConnectionEntry.class);
            Connection mockConnection = mock(Connection.class);

            when(mockEntry.isActive()).thenReturn(false);
            when(mockEntry.isCriticalLock()).thenReturn(false);
            when(mockEntry.isValid()).thenReturn(true);
            when(mockEntry.getConnection()).thenReturn(mockConnection);
            when(mockEntry.getConnectionName()).thenReturn("connection-" + i);
            when(mockEntry.getIdle()).thenReturn(new java.util.concurrent.atomic.AtomicBoolean(true));

            entries.add(mockEntry);
        }
        return entries;
    }
} 
