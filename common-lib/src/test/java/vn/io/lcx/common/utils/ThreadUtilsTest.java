package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ThreadUtilsTest {

    @Test
    void getAllRunningThreadNames_returnsNonEmpty() {
        List<String> threadNames = ThreadUtils.getAllRunningThreadNames();
        assertNotNull(threadNames);
        assertFalse(threadNames.isEmpty(), "There should be at least one running thread");
    }

    @Test
    void getThreadCount_returnsPositive() {
        int count = ThreadUtils.getThreadCount();
        assertTrue(count > 0, "Thread count should be positive, got: " + count);
    }

    @Test
    void isThreadRunning_mainThread_returnsTrue() {
        // The "main" thread should always be running during test execution
        boolean mainRunning = ThreadUtils.isThreadRunning("main");
        assertTrue(mainRunning, "Main thread should be running");
    }

    @Test
    void isThreadRunning_nonExistentThread_returnsFalse() {
        boolean result = ThreadUtils.isThreadRunning("nonexistent-thread-xyz-12345");
        assertFalse(result, "Non-existent thread should not be found");
    }

    @Test
    void isThreadRunning_nullName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> ThreadUtils.isThreadRunning(null));
    }

    @Test
    void isThreadRunning_emptyName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> ThreadUtils.isThreadRunning("  "));
    }
}
