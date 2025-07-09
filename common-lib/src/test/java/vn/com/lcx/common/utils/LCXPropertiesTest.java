package vn.com.lcx.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.lcx.common.constant.CommonConstant;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LCXPropertiesTest {
    private LCXProperties lcxProperties;
    private Properties properties;
    private YamlProperties yamlProperties;

    @BeforeEach
    void setUp() {
        properties = new Properties();
        yamlProperties = mock(YamlProperties.class);
    }

    @Test
    void testGetProperty_withProperties() {
        properties.setProperty("key1", "value1");
        lcxProperties = new LCXProperties(null, properties);
        assertEquals("value1", lcxProperties.getProperty("key1"));
    }

    @Test
    void testGetProperty_withYamlProperties() {
        when(yamlProperties.getProperty("key2")).thenReturn("value2");
        lcxProperties = new LCXProperties(yamlProperties, null);
        assertEquals("value2", lcxProperties.getProperty("key2"));
    }

    @Test
    void testGetProperty_withNoProperties() {
        lcxProperties = new LCXProperties(null, null);
        assertEquals(CommonConstant.EMPTY_STRING, lcxProperties.getProperty("key3"));
    }

    @Test
    void testGetProperty_withFunction() {
        properties.setProperty("key4", "123");
        lcxProperties = new LCXProperties(null, properties);
        Integer result = lcxProperties.getProperty("key4", Integer::parseInt);
        assertEquals(123, result);
    }

    @Test
    void testGetPropertyWithEnvironment_envVariableSet() {
        // Giả lập biến môi trường
        String envKey = "TEST_ENV_VAR";
        String envValue = "envValue";
        // Set biến môi trường tạm thời (không thể set trực tiếp trong Java, nên chỉ test logic)
        properties.setProperty("envKey", "${" + envKey + ":defaultValue}");
        lcxProperties = new LCXProperties(null, properties);
        // Giả lập System.getenv bằng cách mock static nếu cần, ở đây chỉ test default value
        assertEquals("defaultValue", lcxProperties.getPropertyWithEnvironment("envKey"));
    }

    @Test
    void testGetPropertyWithEnvironment_noEnvSyntax() {
        properties.setProperty("plainKey", "plainValue");
        lcxProperties = new LCXProperties(null, properties);
        assertEquals("plainValue", lcxProperties.getPropertyWithEnvironment("plainKey"));
    }

    @Test
    void testGetPropertyWithEnvironment_emptyEnv() {
        properties.setProperty("emptyEnv", "${}");
        lcxProperties = new LCXProperties(null, properties);
        assertEquals(CommonConstant.EMPTY_STRING, lcxProperties.getPropertyWithEnvironment("emptyEnv"));
    }

    @Test
    void testGetPropertyWithEnvironment_withFunction() {
        properties.setProperty("key5", "456");
        lcxProperties = new LCXProperties(null, properties);
        Integer result = lcxProperties.getPropertyWithEnvironment("key5", Integer::parseInt);
        assertEquals(456, result);
    }
} 
