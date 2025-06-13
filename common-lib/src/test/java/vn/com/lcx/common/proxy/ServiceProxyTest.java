package vn.com.lcx.common.proxy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for MethodLoggerProxy.
 */
class ServiceProxyTest {
    private static final Logger log = LoggerFactory.getLogger(ServiceProxyTest.class);

    @Test
    void testClassProxy() {
        log.info("Testing class proxy...");

        // Create the real service
        UserService userService = new UserService("Loc");

        // Create a proxy for the concrete class
        UserService userServiceProxy = ServiceProxyHandler.create(UserService.class, userService);

        // Test the proxy
        String createResult = userServiceProxy.createUser("tuan_loc", "tuan_loc@example.com");
        assertTrue(createResult.contains("tuan_loc"));
        assertTrue(createResult.contains("Total users: 1"));

        String deleteResult = userServiceProxy.deleteUser("tuan_loc");
        assertTrue(deleteResult.contains("tuan_loc"));
        assertTrue(deleteResult.contains("Remaining users: 0"));

        int count = userServiceProxy.getUserCount();
        assertEquals(0, count);
        Assertions.assertThrows(
                UserException.class,
                userServiceProxy::testException
        );
    }

    @Test
    void testClassProxyWithInterface() {
        log.info("Testing class with interface proxy...");

        // Create the real service
        UserService2Impl userService = new UserService2Impl("Loc");

        // Create a proxy for the concrete class
        UserService2 userServiceProxy = ServiceProxyHandler.create(UserService2.class, userService);

        // Test the proxy
        String createResult = userServiceProxy.createUser("tuan_loc", "tuan_loc@example.com");
        assertTrue(createResult.contains("tuan_loc"));
        assertTrue(createResult.contains("Total users: 1"));

        String deleteResult = userServiceProxy.deleteUser("tuan_loc");
        assertTrue(deleteResult.contains("tuan_loc"));
        assertTrue(deleteResult.contains("Remaining users: 0"));

        int count = userServiceProxy.getUserCount();
        assertEquals(0, count);
        Assertions.assertThrows(
                UserException.class,
                userServiceProxy::testException
        );
    }
}
