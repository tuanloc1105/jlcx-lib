package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LCXPropertiesTest {

    private LCXProperties createTestProperties() {
        Map<String, Object> root = new HashMap<>();

        // Nested "app" map to match YAML-style dot-separated key resolution
        Map<String, Object> app = new HashMap<>();
        app.put("name", "my-app");
        app.put("version", "1.0");
        root.put("app", app);

        Map<String, Object> server = new HashMap<>();
        server.put("port", 9090);
        root.put("server", server);

        // Nested "env" map with environment variable syntax value
        Map<String, Object> env = new HashMap<>();
        env.put("value", "${SOME_UNLIKELY_ENV_VAR:default-value}");
        root.put("env", env);

        YamlProperties yamlProps = new YamlProperties(root);
        return new LCXProperties(yamlProps);
    }

    @Test
    void getProperty_returnsValue() {
        LCXProperties props = createTestProperties();
        assertEquals("my-app", props.getProperty("app.name"));
        assertEquals("1.0", props.getProperty("app.version"));
        assertEquals("9090", props.getProperty("server.port"));
    }

    @Test
    void getProperty_nonExistentKey_returnsNull() {
        LCXProperties props = createTestProperties();
        assertNull(props.getProperty("nonexistent"));
    }

    @Test
    void getProperty_nullYamlProperties_returnsNull() {
        LCXProperties props = new LCXProperties();
        assertNull(props.getProperty("any.key"));
    }

    @Test
    void getPropertyWithEnvironment_withDefault() {
        LCXProperties props = createTestProperties();

        // When the env variable is not set, it should return the default value
        String result = props.getPropertyWithEnvironment("env.value");
        assertEquals("default-value", result);
    }

    @Test
    void getPropertyWithEnvironment_plainValue() {
        LCXProperties props = createTestProperties();

        // Non-env variable values should be returned as-is (with empty string appended)
        String result = props.getPropertyWithEnvironment("app.name");
        assertEquals("my-app", result);
    }

    @Test
    void getProperty_withFunction_transforms() {
        LCXProperties props = createTestProperties();

        Integer port = props.getProperty("server.port", Integer::parseInt);
        assertEquals(9090, port);
    }

    @Test
    void getPropertyWithEnvironment_withFunction_transforms() {
        LCXProperties props = createTestProperties();

        // The env value should resolve to the default "default-value"
        String result = props.getPropertyWithEnvironment("env.value", val -> val.toUpperCase());
        assertEquals("DEFAULT-VALUE", result);
    }
}
