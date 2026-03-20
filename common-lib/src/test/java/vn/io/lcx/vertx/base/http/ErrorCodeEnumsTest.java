package vn.io.lcx.vertx.base.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vn.io.lcx.vertx.base.enums.ErrorCode;
import vn.io.lcx.vertx.base.enums.ErrorCodeEnums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ErrorCodeEnums")
class ErrorCodeEnumsTest {

    @Nested
    @DisplayName("SUCCESS")
    class SuccessTests {

        @Test
        @DisplayName("success_is200")
        void success_is200() {
            assertEquals(200, ErrorCodeEnums.SUCCESS.getHttpCode());
            assertEquals(100000, ErrorCodeEnums.SUCCESS.getCode());
            assertEquals("Success", ErrorCodeEnums.SUCCESS.getMessage());
        }
    }

    @Nested
    @DisplayName("INVALID_REQUEST")
    class InvalidRequestTests {

        @Test
        @DisplayName("invalidRequest_is400")
        void invalidRequest_is400() {
            assertEquals(400, ErrorCodeEnums.INVALID_REQUEST.getHttpCode());
            assertEquals(100001, ErrorCodeEnums.INVALID_REQUEST.getCode());
            assertEquals("Invalid request", ErrorCodeEnums.INVALID_REQUEST.getMessage());
        }
    }

    @Nested
    @DisplayName("INTERNAL_ERROR")
    class InternalErrorTests {

        @Test
        @DisplayName("internalError_is500")
        void internalError_is500() {
            assertEquals(500, ErrorCodeEnums.INTERNAL_ERROR.getHttpCode());
            assertEquals(100002, ErrorCodeEnums.INTERNAL_ERROR.getCode());
            assertEquals("Internal error", ErrorCodeEnums.INTERNAL_ERROR.getMessage());
        }
    }

    @Nested
    @DisplayName("DATA_NOT_FOUND")
    class DataNotFoundTests {

        @Test
        @DisplayName("dataNotFound_is404")
        void dataNotFound_is404() {
            assertEquals(404, ErrorCodeEnums.DATA_NOT_FOUND.getHttpCode());
            assertEquals(100003, ErrorCodeEnums.DATA_NOT_FOUND.getCode());
            assertEquals("Data not found", ErrorCodeEnums.DATA_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("DATA_ERROR")
    class DataErrorTests {

        @Test
        @DisplayName("dataError_is400")
        void dataError_is400() {
            assertEquals(400, ErrorCodeEnums.DATA_ERROR.getHttpCode());
            assertEquals(100004, ErrorCodeEnums.DATA_ERROR.getCode());
            assertEquals("Data error", ErrorCodeEnums.DATA_ERROR.getMessage());
        }
    }

    @Nested
    @DisplayName("ErrorCode interface")
    class ErrorCodeInterfaceTests {

        @Test
        @DisplayName("implementsErrorCodeInterface")
        void implementsErrorCodeInterface() {
            assertTrue(ErrorCodeEnums.SUCCESS instanceof ErrorCode,
                    "ErrorCodeEnums should implement ErrorCode interface");
        }

        @Test
        @DisplayName("allEnums_implementErrorCode")
        void allEnums_implementErrorCode() {
            for (ErrorCodeEnums value : ErrorCodeEnums.values()) {
                assertTrue(value instanceof ErrorCode,
                        value.name() + " should implement ErrorCode");
                assertTrue(value.getHttpCode() > 0,
                        value.name() + " should have a positive HTTP code");
                assertTrue(value.getCode() > 0,
                        value.name() + " should have a positive error code");
                assertTrue(value.getMessage() != null && !value.getMessage().isEmpty(),
                        value.name() + " should have a non-empty message");
            }
        }
    }

    @Nested
    @DisplayName("Enum completeness")
    class EnumCompleteness {

        @Test
        @DisplayName("valueOf_returnsCorrectEnum")
        void valueOf_returnsCorrectEnum() {
            assertEquals(ErrorCodeEnums.SUCCESS, ErrorCodeEnums.valueOf("SUCCESS"));
            assertEquals(ErrorCodeEnums.INVALID_REQUEST, ErrorCodeEnums.valueOf("INVALID_REQUEST"));
            assertEquals(ErrorCodeEnums.INTERNAL_ERROR, ErrorCodeEnums.valueOf("INTERNAL_ERROR"));
            assertEquals(ErrorCodeEnums.DATA_NOT_FOUND, ErrorCodeEnums.valueOf("DATA_NOT_FOUND"));
            assertEquals(ErrorCodeEnums.DATA_ERROR, ErrorCodeEnums.valueOf("DATA_ERROR"));
        }

        @Test
        @DisplayName("values_contains5Enums")
        void values_contains5Enums() {
            assertEquals(5, ErrorCodeEnums.values().length);
        }
    }
}
