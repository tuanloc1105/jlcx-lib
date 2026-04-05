package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionUtilsTest {

    @Test
    void getStackTrace_returnsStackTraceString() {
        RuntimeException exception = new RuntimeException("test error");
        String stackTrace = ExceptionUtils.getStackTrace(exception);

        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("test error"));
        assertTrue(stackTrace.contains("RuntimeException"));
        assertTrue(stackTrace.contains("ExceptionUtilsTest"));
    }

    @Test
    void getStackTrace_nestedCause() {
        RuntimeException cause = new RuntimeException("root cause");
        RuntimeException wrapper = new RuntimeException("wrapper error", cause);

        String stackTrace = ExceptionUtils.getStackTrace(wrapper);

        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("wrapper error"));
        assertTrue(stackTrace.contains("root cause"));
        assertTrue(stackTrace.contains("Caused by"));
    }

    @Test
    void getStackTrace_nullInput_throwsNPE() {
        // The method calls throwable.printStackTrace(printWriter) which will throw NPE
        // if throwable is null
        assertThrows(NullPointerException.class, () -> ExceptionUtils.getStackTrace(null));
    }

    @Test
    void getStackTrace_containsMethodName() {
        Exception exception = new IllegalArgumentException("bad argument");
        String stackTrace = ExceptionUtils.getStackTrace(exception);

        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("IllegalArgumentException"));
        assertTrue(stackTrace.contains("bad argument"));
    }

    @Test
    void getStackTrace_exceptionWithoutMessage() {
        RuntimeException exception = new RuntimeException();
        String stackTrace = ExceptionUtils.getStackTrace(exception);

        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("RuntimeException"));
    }

    @Test
    void getStackTrace_deeplyNestedCause() {
        Exception level1 = new Exception("level 1");
        Exception level2 = new RuntimeException("level 2", level1);
        Exception level3 = new IllegalStateException("level 3", level2);

        String stackTrace = ExceptionUtils.getStackTrace(level3);

        assertNotNull(stackTrace);
        assertTrue(stackTrace.contains("level 1"));
        assertTrue(stackTrace.contains("level 2"));
        assertTrue(stackTrace.contains("level 3"));
        assertTrue(stackTrace.contains("IllegalStateException"));
    }
}
