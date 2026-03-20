package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlPropertiesTest {

    private YamlProperties createTestProperties() {
        Map<String, Object> root = new HashMap<>();

        // Simple key
        root.put("name", "test-app");

        // Nested
        Map<String, Object> server = new HashMap<>();
        server.put("port", 8080);
        server.put("host", "localhost");
        root.put("server", server);

        // Deeply nested
        Map<String, Object> database = new HashMap<>();
        Map<String, Object> connection = new HashMap<>();
        Map<String, Object> pool = new HashMap<>();
        pool.put("size", 10);
        connection.put("pool", pool);
        connection.put("url", "jdbc:h2:mem:test");
        database.put("connection", connection);
        root.put("database", database);

        return new YamlProperties(root);
    }

    @Test
    void getProperty_simpleKey_returnsValue() {
        YamlProperties props = createTestProperties();
        assertEquals("test-app", props.getProperty("name"));
    }

    @Test
    void getProperty_nestedKey_returnsValue() {
        YamlProperties props = createTestProperties();
        assertEquals("8080", props.getProperty("server.port"));
        assertEquals("localhost", props.getProperty("server.host"));
    }

    @Test
    void getProperty_nonExistentKey_returnsNull() {
        YamlProperties props = createTestProperties();
        assertNull(props.getProperty("nonexistent"));
        assertNull(props.getProperty("server.nonexistent"));
    }

    @Test
    void getProperty_withDefault_returnsDefault() {
        YamlProperties props = createTestProperties();
        assertEquals("fallback", props.getProperty("missing.key", "fallback"));
        // Existing key should not return default
        assertEquals("test-app", props.getProperty("name", "fallback"));
    }

    @Test
    void getProperty_deeplyNested_returnsValue() {
        YamlProperties props = createTestProperties();
        assertEquals("10", props.getProperty("database.connection.pool.size"));
        assertEquals("jdbc:h2:mem:test", props.getProperty("database.connection.url"));
    }
}
