package vn.io.lcx.processor.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SourceParameterInfo}.
 * <p>
 * SourceParameterInfo holds metadata about a single source parameter of a
 * mapping method: parameter name, fully qualified type, and its fields.
 */
class SourceParameterInfoTest {

    @Test
    void constructorSetsAllFields() {
        SourceParameterInfo info = new SourceParameterInfo(
                "userDto", "com.example.UserDto", Collections.emptyList());

        assertEquals("userDto", info.getParamName());
        assertEquals("com.example.UserDto", info.getParamType());
        assertNotNull(info.getFields());
        assertTrue(info.getFields().isEmpty());
    }

    @Test
    void getParamNameReturnsCorrectValue() {
        SourceParameterInfo info = new SourceParameterInfo(
                "source", "MyClass", Collections.emptyList());

        assertEquals("source", info.getParamName());
    }

    @Test
    void getParamTypeReturnsCorrectValue() {
        SourceParameterInfo info = new SourceParameterInfo(
                "dto", "vn.io.lcx.dto.OrderDto", Collections.emptyList());

        assertEquals("vn.io.lcx.dto.OrderDto", info.getParamType());
    }

    @Test
    void acceptsNullValues() {
        SourceParameterInfo info = new SourceParameterInfo(null, null, null);

        assertEquals(null, info.getParamName());
        assertEquals(null, info.getParamType());
        assertEquals(null, info.getFields());
    }
}
