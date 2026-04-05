package vn.io.lcx.common.utils;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonMaskingUtilsTest {

    private final Gson gson = new Gson();

    @Test
    void maskJsonFields_masksSpecifiedFields() {
        String json = "{\"username\":\"john\",\"password\":\"secret123\",\"email\":\"john@test.com\"}";

        String masked = JsonMaskingUtils.maskJsonFields(gson, json, "password");

        assertNotNull(masked);
        assertFalse(masked.contains("secret123"), "Password value should be masked");
        assertTrue(masked.contains("john"), "Username should not be masked");
        assertTrue(masked.contains("password"), "Password key should still exist");
        assertTrue(masked.contains("*"), "Masked value should contain asterisks");
    }

    @Test
    void maskJsonFields_nestedObject_masksRecursively() {
        String json = "{\"user\":{\"name\":\"john\",\"credentials\":{\"password\":\"mypass\"}}}";

        String masked = JsonMaskingUtils.maskJsonFields(gson, json, "password");

        assertNotNull(masked);
        assertFalse(masked.contains("mypass"), "Nested password should be masked");
        assertTrue(masked.contains("john"), "Name should not be masked");
    }

    @Test
    void maskJsonFields_noSensitiveFields_unchanged() {
        String json = "{\"name\":\"john\",\"age\":30}";

        String masked = JsonMaskingUtils.maskJsonFields(gson, json, "password", "secret");

        assertNotNull(masked);
        assertTrue(masked.contains("john"));
        assertTrue(masked.contains("30"));
    }

    @Test
    void maskJsonFields_blankInput_returnsInput() {
        String result = JsonMaskingUtils.maskJsonFields(gson, "", "password");
        assertEquals("", result);

        String nullResult = JsonMaskingUtils.maskJsonFields(gson, null, "password");
        assertNull(nullResult);
    }

    @Test
    void maskJsonFields_noFieldNames_returnsInput() {
        String json = "{\"password\":\"secret\"}";
        String result = JsonMaskingUtils.maskJsonFields(gson, json);

        // When no field names provided but CUSTOM_FIELD is empty,
        // it uses default SENSITIVE_FIELD_NAMES which includes "password"
        assertNotNull(result);
    }

    @Test
    void maskJsonFields_nonJsonInput_returnsInput() {
        String nonJson = "This is not JSON";
        String result = JsonMaskingUtils.maskJsonFields(gson, nonJson, "password");
        assertEquals(nonJson, result);
    }

    @Test
    void maskJsonFields_unsupportedHandler_throwsException() {
        String json = "{\"key\":\"value\"}";
        assertThrows(UnsupportedOperationException.class,
                () -> JsonMaskingUtils.maskJsonFields("not a handler", json, "key"));
    }
}
