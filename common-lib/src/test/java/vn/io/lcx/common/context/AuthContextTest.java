package vn.io.lcx.common.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AuthContextTest {

    @AfterEach
    void cleanup() {
        AuthContext.clear();
    }

    @Test
    void set_and_get_returnsValue() {
        String authData = "user-token-abc";
        AuthContext.set(authData);

        Object result = AuthContext.get();

        assertEquals("user-token-abc", result);
    }

    @Test
    void get_withClass_returnsCastedValue() {
        String authData = "typed-token";
        AuthContext.set(authData);

        String result = AuthContext.get(String.class);

        assertNotNull(result);
        assertEquals("typed-token", result);
    }

    @Test
    void clear_removesValue() {
        AuthContext.set("some-value");
        assertNotNull(AuthContext.get());

        AuthContext.clear();

        assertNull(AuthContext.get());
    }

    @Test
    void get_afterClear_returnsNull() {
        AuthContext.set("temporary-data");
        AuthContext.clear();

        Object result = AuthContext.get();

        assertNull(result, "Should return null after clear");
    }

    @Test
    void set_differentThreads_isolatedValues() throws Exception {
        // Set value in main thread
        AuthContext.set("main-thread-value");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Object> otherThreadValue = new AtomicReference<>();
        AtomicReference<Object> otherThreadAfterSet = new AtomicReference<>();

        Thread otherThread = new Thread(() -> {
            // Should NOT see the main thread's value
            otherThreadValue.set(AuthContext.get());

            // Set a different value in this thread
            AuthContext.set("other-thread-value");
            otherThreadAfterSet.set(AuthContext.get());

            AuthContext.clear();
            latch.countDown();
        });
        otherThread.start();
        latch.await();

        // Other thread should not have seen main thread's value
        assertNull(otherThreadValue.get(),
                "Other thread should not see main thread's AuthContext value");

        // Other thread should have its own value
        assertEquals("other-thread-value", otherThreadAfterSet.get(),
                "Other thread should have its own isolated value");

        // Main thread's value should be unaffected
        assertEquals("main-thread-value", AuthContext.get(),
                "Main thread's AuthContext should be unchanged");
    }
}
