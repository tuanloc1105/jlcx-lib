package vn.io.lcx.common.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void builder_setsAllFields() {
        Map<String, List<String>> headers = Map.of("Content-Type", List.of("application/json"));

        Response<String> response = Response.<String>builder()
                .code(200)
                .msg("OK")
                .response("Hello")
                .responseHeaders(headers)
                .errorResponse(null)
                .build();

        assertEquals(200, response.getCode());
        assertEquals("OK", response.getMsg());
        assertEquals("Hello", response.getResponse());
        assertEquals(headers, response.getResponseHeaders());
        assertNull(response.getErrorResponse());
    }

    @Test
    void builder_withErrorResponse() {
        Response<Object> response = Response.builder()
                .code(500)
                .msg("Internal Server Error")
                .errorResponse("Something went wrong")
                .build();

        assertEquals(500, response.getCode());
        assertEquals("Internal Server Error", response.getMsg());
        assertNull(response.getResponse());
        assertEquals("Something went wrong", response.getErrorResponse());
    }

    @Test
    void getters_returnCorrectValues() {
        Response<Integer> response = Response.<Integer>builder()
                .code(201)
                .msg("Created")
                .response(42)
                .build();

        assertEquals(201, response.getCode());
        assertEquals("Created", response.getMsg());
        assertEquals(42, response.getResponse());
        assertNull(response.getResponseHeaders());
        assertNull(response.getErrorResponse());
    }

    @Test
    void setters_updateValues() {
        Response<String> response = Response.<String>builder()
                .code(200)
                .msg("OK")
                .build();

        response.setCode(404);
        response.setMsg("Not Found");
        response.setResponse("missing");
        response.setErrorResponse("Resource not found");
        response.setResponseHeaders(Map.of("X-Custom", List.of("value")));

        assertEquals(404, response.getCode());
        assertEquals("Not Found", response.getMsg());
        assertEquals("missing", response.getResponse());
        assertEquals("Resource not found", response.getErrorResponse());
        assertNotNull(response.getResponseHeaders());
    }

    @Test
    void toString_returnsNonNull() {
        Response<String> response = Response.<String>builder()
                .code(200)
                .msg("OK")
                .response("data")
                .build();

        String str = response.toString();
        assertNotNull(str);
        assertTrue(str.contains("200"));
        assertTrue(str.contains("OK"));
        assertTrue(str.contains("data"));
        assertTrue(str.startsWith("Response{"));
    }
}
