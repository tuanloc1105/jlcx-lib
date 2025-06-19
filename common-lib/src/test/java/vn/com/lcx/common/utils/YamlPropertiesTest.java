package vn.com.lcx.common.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlPropertiesTest {
    @Test
    void testGetProperty_flatKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("simpleKey", "simpleValue");
        YamlProperties yamlProperties = new YamlProperties(map);
        assertEquals("simpleValue", yamlProperties.getProperty("simpleKey"));
    }

    @Test
    void testGetProperty_nestedKey() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("innerKey", "innerValue");
        Map<String, Object> map = new HashMap<>();
        map.put("outer", nested);
        YamlProperties yamlProperties = new YamlProperties(map);
        assertEquals("innerValue", yamlProperties.getProperty("outer.innerKey"));
    }

    @Test
    void testGetProperty_keyNotFound() {
        Map<String, Object> map = new HashMap<>();
        YamlProperties yamlProperties = new YamlProperties(map);
        assertNull(yamlProperties.getProperty("notExist"));
    }

    @Test
    void testGetProperty_withDefaultValue() {
        Map<String, Object> map = new HashMap<>();
        YamlProperties yamlProperties = new YamlProperties(map);
        assertEquals("default", yamlProperties.getProperty("notExist", "default"));
    }

    @Test
    void testGetProperty_nestedKeyNotFound() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("innerKey", "innerValue");
        Map<String, Object> map = new HashMap<>();
        map.put("outer", nested);
        YamlProperties yamlProperties = new YamlProperties(map);
        assertNull(yamlProperties.getProperty("outer.notExist"));
    }
} 
