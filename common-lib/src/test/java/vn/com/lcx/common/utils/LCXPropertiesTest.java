package vn.com.lcx.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.lcx.common.constant.CommonConstant;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//TODO fix
class LCXPropertiesTest {
    private LCXProperties lcxProperties;
    private YamlProperties yamlProperties;

    @BeforeEach
    void setUp() {
        yamlProperties = mock(YamlProperties.class);
    }

    @Test
    void testGetProperty_withYamlProperties() {
        when(yamlProperties.getProperty("key2")).thenReturn("value2");
        lcxProperties = new LCXProperties(yamlProperties);
        assertEquals("value2", lcxProperties.getProperty("key2"));
    }

    @Test
    void testGetProperty_withNoProperties() {
        lcxProperties = new LCXProperties(null);
        assertEquals(CommonConstant.EMPTY_STRING, lcxProperties.getProperty("key3"));
    }

} 
