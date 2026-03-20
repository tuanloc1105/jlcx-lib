package vn.io.lcx.vertx.base.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vn.io.lcx.vertx.base.http.response.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ResponseEntity")
class ResponseEntityTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("constructor_setsStatusAndBody")
        void constructor_setsStatusAndBody() {
            ResponseEntity<String> entity = new ResponseEntity<>(200, "OK");

            assertEquals(200, entity.getStatus());
            assertEquals("OK", entity.getResponse());
        }

        @Test
        @DisplayName("constructor_withNullBody_allowsNull")
        void constructor_withNullBody_allowsNull() {
            ResponseEntity<String> entity = new ResponseEntity<>(204, null);

            assertEquals(204, entity.getStatus());
            assertNull(entity.getResponse());
        }

        @Test
        @DisplayName("constructor_withComplexType_setsBody")
        void constructor_withComplexType_setsBody() {
            List<String> body = List.of("item1", "item2");
            ResponseEntity<List<String>> entity = new ResponseEntity<>(200, body);

            assertEquals(200, entity.getStatus());
            assertEquals(2, entity.getResponse().size());
            assertEquals("item1", entity.getResponse().get(0));
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("getStatus_returnsStatus")
        void getStatus_returnsStatus() {
            ResponseEntity<String> entity = new ResponseEntity<>(201, "Created");

            assertEquals(201, entity.getStatus());
        }

        @Test
        @DisplayName("getResponse_returnsBody")
        void getResponse_returnsBody() {
            Map<String, Object> body = Map.of("key", "value");
            ResponseEntity<Map<String, Object>> entity = new ResponseEntity<>(200, body);

            assertEquals(body, entity.getResponse());
        }
    }

    @Nested
    @DisplayName("Setters")
    class SetterTests {

        @Test
        @DisplayName("setStatus_updatesStatus")
        void setStatus_updatesStatus() {
            ResponseEntity<String> entity = new ResponseEntity<>(200, "OK");
            entity.setStatus(404);

            assertEquals(404, entity.getStatus());
        }

        @Test
        @DisplayName("setResponse_updatesBody")
        void setResponse_updatesBody() {
            ResponseEntity<String> entity = new ResponseEntity<>(200, "Original");
            entity.setResponse("Updated");

            assertEquals("Updated", entity.getResponse());
        }
    }

    @Nested
    @DisplayName("Static factory methods")
    class FactoryMethodTests {

        @Test
        @DisplayName("of_createsEntityWithStatusAndBody")
        void of_createsEntityWithStatusAndBody() {
            ResponseEntity<String> entity = ResponseEntity.of(201, "Created");

            assertEquals(201, entity.getStatus());
            assertEquals("Created", entity.getResponse());
        }

        @Test
        @DisplayName("ok_createsEntityWith200Status")
        void ok_createsEntityWith200Status() {
            ResponseEntity<String> entity = ResponseEntity.ok("Success");

            assertEquals(200, entity.getStatus());
            assertEquals("Success", entity.getResponse());
        }

        @Test
        @DisplayName("ok_withComplexBody_setsBody")
        void ok_withComplexBody_setsBody() {
            List<Integer> data = List.of(1, 2, 3);
            ResponseEntity<List<Integer>> entity = ResponseEntity.ok(data);

            assertEquals(200, entity.getStatus());
            assertEquals(3, entity.getResponse().size());
        }

        @Test
        @DisplayName("badRequest_createsEntityWith400Status")
        void badRequest_createsEntityWith400Status() {
            ResponseEntity<String> entity = ResponseEntity.badRequest("Invalid input");

            assertEquals(400, entity.getStatus());
            assertEquals("Invalid input", entity.getResponse());
        }

        @Test
        @DisplayName("internalServerError_createsEntityWith500Status")
        void internalServerError_createsEntityWith500Status() {
            ResponseEntity<String> entity = ResponseEntity.internalServerError("Something broke");

            assertEquals(500, entity.getStatus());
            assertEquals("Something broke", entity.getResponse());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString_containsStatusAndResponse")
        void toString_containsStatusAndResponse() {
            ResponseEntity<String> entity = new ResponseEntity<>(200, "OK");

            String result = entity.toString();

            assertNotNull(result);
            assertTrue(result.contains("200"));
            assertTrue(result.contains("OK"));
            assertTrue(result.contains("ResponseEntity"));
        }
    }
}
