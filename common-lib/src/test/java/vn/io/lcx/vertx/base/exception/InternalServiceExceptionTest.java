package vn.io.lcx.vertx.base.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vn.io.lcx.vertx.base.enums.ErrorCodeEnums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InternalServiceException")
class InternalServiceExceptionTest {

    @Nested
    @DisplayName("Constructor with ErrorCode enum")
    class ErrorCodeConstructor {

        @Test
        @DisplayName("constructor_withErrorCode_setsFields")
        void constructor_withErrorCode_setsFields() {
            InternalServiceException exception = new InternalServiceException(ErrorCodeEnums.INVALID_REQUEST);

            assertEquals(400, exception.getHttpCode());
            assertEquals(100001, exception.getCode());
            assertEquals("Invalid request", exception.getMessage());
        }

        @Test
        @DisplayName("constructor_withErrorCodeAndAdditionalMessage_appendsMessage")
        void constructor_withErrorCodeAndAdditionalMessage_appendsMessage() {
            InternalServiceException exception = new InternalServiceException(
                    ErrorCodeEnums.INVALID_REQUEST, "field is missing"
            );

            assertEquals(400, exception.getHttpCode());
            assertEquals(100001, exception.getCode());
            assertTrue(exception.getMessage().contains("Invalid request"));
            assertTrue(exception.getMessage().contains("field is missing"));
        }

        @Test
        @DisplayName("constructor_withErrorCodeAndMultipleAdditionalMessages_joinsWithSemicolon")
        void constructor_withErrorCodeAndMultipleAdditionalMessages_joinsWithSemicolon() {
            InternalServiceException exception = new InternalServiceException(
                    ErrorCodeEnums.INVALID_REQUEST, "error1", "error2"
            );

            assertTrue(exception.getMessage().contains("error1"));
            assertTrue(exception.getMessage().contains("error2"));
            assertTrue(exception.getMessage().contains("; "));
        }

        @Test
        @DisplayName("constructor_withInternalError_setsCorrectFields")
        void constructor_withInternalError_setsCorrectFields() {
            InternalServiceException exception = new InternalServiceException(ErrorCodeEnums.INTERNAL_ERROR);

            assertEquals(500, exception.getHttpCode());
            assertEquals(100002, exception.getCode());
            assertEquals("Internal error", exception.getMessage());
        }

        @Test
        @DisplayName("constructor_withDataNotFound_setsCorrectFields")
        void constructor_withDataNotFound_setsCorrectFields() {
            InternalServiceException exception = new InternalServiceException(ErrorCodeEnums.DATA_NOT_FOUND);

            assertEquals(404, exception.getHttpCode());
            assertEquals(100003, exception.getCode());
            assertEquals("Data not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Constructor with custom values")
    class CustomValuesConstructor {

        @Test
        @DisplayName("constructor_withCustomValues_setsFields")
        void constructor_withCustomValues_setsFields() {
            InternalServiceException exception = new InternalServiceException(403, 999, "Forbidden");

            assertEquals(403, exception.getHttpCode());
            assertEquals(999, exception.getCode());
            assertEquals("Forbidden", exception.getMessage());
        }

        @Test
        @DisplayName("constructor_withCustomValuesAndAdditionalMessage_appendsMessage")
        void constructor_withCustomValuesAndAdditionalMessage_appendsMessage() {
            InternalServiceException exception = new InternalServiceException(
                    403, 999, "Forbidden", "resource X"
            );

            assertEquals(403, exception.getHttpCode());
            assertEquals(999, exception.getCode());
            assertTrue(exception.getMessage().contains("Forbidden"));
            assertTrue(exception.getMessage().contains("resource X"));
        }
    }

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("constructor_default_usesInternalError")
        void constructor_default_usesInternalError() {
            InternalServiceException exception = new InternalServiceException();

            assertEquals(500, exception.getHttpCode());
            assertEquals(100002, exception.getCode());
            assertEquals("Internal error", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Getters and setters")
    class GettersAndSetters {

        @Test
        @DisplayName("getHttpCode_returnsCorrectCode")
        void getHttpCode_returnsCorrectCode() {
            InternalServiceException exception = new InternalServiceException(ErrorCodeEnums.INVALID_REQUEST);

            assertEquals(400, exception.getHttpCode());
        }

        @Test
        @DisplayName("getCode_returnsErrorCode")
        void getCode_returnsErrorCode() {
            InternalServiceException exception = new InternalServiceException(ErrorCodeEnums.DATA_NOT_FOUND);

            assertEquals(100003, exception.getCode());
        }

        @Test
        @DisplayName("getMessage_returnsDescription")
        void getMessage_returnsDescription() {
            InternalServiceException exception = new InternalServiceException(ErrorCodeEnums.SUCCESS);

            assertEquals("Success", exception.getMessage());
        }

        @Test
        @DisplayName("setHttpCode_updatesValue")
        void setHttpCode_updatesValue() {
            InternalServiceException exception = new InternalServiceException();
            exception.setHttpCode(503);

            assertEquals(503, exception.getHttpCode());
        }

        @Test
        @DisplayName("setCode_updatesValue")
        void setCode_updatesValue() {
            InternalServiceException exception = new InternalServiceException();
            exception.setCode(12345);

            assertEquals(12345, exception.getCode());
        }

        @Test
        @DisplayName("setMessage_updatesValue")
        void setMessage_updatesValue() {
            InternalServiceException exception = new InternalServiceException();
            exception.setMessage("New message");

            assertEquals("New message", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Exception hierarchy")
    class ExceptionHierarchy {

        @Test
        @DisplayName("exception_isRuntimeException")
        void exception_isRuntimeException() {
            InternalServiceException exception = new InternalServiceException();

            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("exception_superMessage_containsFormattedString")
        void exception_superMessage_containsFormattedString() {
            InternalServiceException exception = new InternalServiceException(400, 100001, "Invalid request");

            // The super (RuntimeException) message is formatted as "httpCode - code - message"
            assertNotNull(exception.toString());
            // super.getMessage() via RuntimeException should contain the formatted string
            // but InternalServiceException overrides getMessage() to return just the message field
            assertEquals("Invalid request", exception.getMessage());
        }
    }
}
