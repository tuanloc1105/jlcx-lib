package vn.io.lcx.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesUtilsTest {

    @Test
    void getProperties_validYaml_returnsProperties() {
        LCXProperties props = PropertiesUtils.getProperties(
                getClass().getClassLoader(), "application-test.yaml");

        assertNotNull(props);
        assertEquals("8888", props.getProperty("server.port"));
        assertEquals("localhost", props.getProperty("server.host"));
        assertEquals("h2", props.getProperty("database.type"));
        assertEquals("sa", props.getProperty("database.username"));
    }

    @Test
    void getProperties_nonExistentFile_returnsEmpty() {
        LCXProperties props = PropertiesUtils.getProperties("non-existent-file.yaml");
        assertNotNull(props);
        assertNull(props.getProperty("server.port"));
    }

    @Test
    void getProperties_nonExistentResource_returnsEmptyProperties() {
        LCXProperties props = PropertiesUtils.getProperties(
                getClass().getClassLoader(), "does-not-exist.yaml");
        assertNotNull(props);
        // The properties should be empty but not throw
        assertNull(props.getProperty("any.key"));
    }

    @Test
    void emptyProperty_returnsEmptyProperties() {
        LCXProperties props = PropertiesUtils.emptyProperty();
        assertNotNull(props);
        assertNull(props.getProperty("any.key"));
    }
}
