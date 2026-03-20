package vn.io.lcx.vertx.base.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vn.io.lcx.vertx.base.http.response.CommonResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("CommonResponse")
class CommonResponseTest {

    @Nested
    @DisplayName("Builder pattern")
    class BuilderTests {

        @Test
        @DisplayName("builder_setsAllFields")
        void builder_setsAllFields() {
            CommonResponse response = CommonResponse.builder()
                    .trace("trace-123")
                    .errorCode(100001)
                    .errorDescription("Invalid request")
                    .httpCode(400)
                    .build();

            assertEquals("trace-123", response.getTrace());
            assertEquals(100001, response.getErrorCode());
            assertEquals("Invalid request", response.getErrorDescription());
            assertEquals(400, response.getHttpCode());
        }

        @Test
        @DisplayName("builder_withNullTrace_allowsNull")
        void builder_withNullTrace_allowsNull() {
            CommonResponse response = CommonResponse.builder()
                    .trace(null)
                    .errorCode(0)
                    .errorDescription(null)
                    .httpCode(200)
                    .build();

            assertNull(response.getTrace());
            assertEquals(0, response.getErrorCode());
            assertNull(response.getErrorDescription());
            assertEquals(200, response.getHttpCode());
        }

        @Test
        @DisplayName("builder_returnsNonNull")
        void builder_returnsNonNull() {
            CommonResponse.CommonResponseBuilder builder = CommonResponse.builder();

            assertNotNull(builder);
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("defaultConstructor_createsInstance")
        void defaultConstructor_createsInstance() {
            CommonResponse response = new CommonResponse();

            assertNotNull(response);
            assertNull(response.getTrace());
            assertEquals(0, response.getErrorCode());
            assertNull(response.getErrorDescription());
            assertEquals(0, response.getHttpCode());
        }

        @Test
        @DisplayName("allArgsConstructor_setsAllFields")
        void allArgsConstructor_setsAllFields() {
            CommonResponse response = new CommonResponse("trace-456", 100002, "Internal error", 500);

            assertEquals("trace-456", response.getTrace());
            assertEquals(100002, response.getErrorCode());
            assertEquals("Internal error", response.getErrorDescription());
            assertEquals(500, response.getHttpCode());
        }
    }

    @Nested
    @DisplayName("Getters and setters")
    class GettersAndSetters {

        @Test
        @DisplayName("getters_returnCorrectValues")
        void getters_returnCorrectValues() {
            CommonResponse response = new CommonResponse("traceId", 100000, "Success", 200);

            assertEquals("traceId", response.getTrace());
            assertEquals(100000, response.getErrorCode());
            assertEquals("Success", response.getErrorDescription());
            assertEquals(200, response.getHttpCode());
        }

        @Test
        @DisplayName("setTrace_updatesValue")
        void setTrace_updatesValue() {
            CommonResponse response = new CommonResponse();
            response.setTrace("new-trace");

            assertEquals("new-trace", response.getTrace());
        }

        @Test
        @DisplayName("setErrorCode_updatesValue")
        void setErrorCode_updatesValue() {
            CommonResponse response = new CommonResponse();
            response.setErrorCode(999);

            assertEquals(999, response.getErrorCode());
        }

        @Test
        @DisplayName("setErrorDescription_updatesValue")
        void setErrorDescription_updatesValue() {
            CommonResponse response = new CommonResponse();
            response.setErrorDescription("Something went wrong");

            assertEquals("Something went wrong", response.getErrorDescription());
        }

        @Test
        @DisplayName("setHttpCode_updatesValue")
        void setHttpCode_updatesValue() {
            CommonResponse response = new CommonResponse();
            response.setHttpCode(404);

            assertEquals(404, response.getHttpCode());
        }
    }

    @Nested
    @DisplayName("Error response scenarios")
    class ErrorResponseScenarios {

        @Test
        @DisplayName("errorResponse_containsTraceId")
        void errorResponse_containsTraceId() {
            String traceId = "abc-def-123";
            CommonResponse response = CommonResponse.builder()
                    .trace(traceId)
                    .errorCode(100002)
                    .errorDescription("Internal error")
                    .httpCode(500)
                    .build();

            assertEquals(traceId, response.getTrace());
            assertEquals(500, response.getHttpCode());
        }

        @Test
        @DisplayName("successResponse_has200HttpCode")
        void successResponse_has200HttpCode() {
            CommonResponse response = CommonResponse.builder()
                    .trace("trace-001")
                    .errorCode(100000)
                    .errorDescription("Success")
                    .httpCode(200)
                    .build();

            assertEquals(200, response.getHttpCode());
            assertEquals(100000, response.getErrorCode());
            assertEquals("Success", response.getErrorDescription());
        }

        @Test
        @DisplayName("serializable_instanceOfSerializable")
        void serializable_instanceOfSerializable() {
            CommonResponse response = new CommonResponse();

            assertTrue_serializable(response);
        }

        private void assertTrue_serializable(Object obj) {
            org.junit.jupiter.api.Assertions.assertTrue(
                    obj instanceof java.io.Serializable,
                    "CommonResponse should implement Serializable"
            );
        }
    }
}
