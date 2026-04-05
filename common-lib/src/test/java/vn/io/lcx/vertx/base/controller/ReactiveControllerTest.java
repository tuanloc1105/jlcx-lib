package vn.io.lcx.vertx.base.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.io.lcx.common.constant.CommonConstant;
import vn.io.lcx.vertx.base.constant.VertxBaseConstant;
import vn.io.lcx.vertx.base.enums.ErrorCodeEnums;
import vn.io.lcx.vertx.base.exception.InternalServiceException;
import vn.io.lcx.vertx.base.http.request.CommonRequest;
import vn.io.lcx.vertx.base.http.response.CommonResponse;
import vn.io.lcx.vertx.base.http.response.FileEntity;
import vn.io.lcx.vertx.base.http.response.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ReactiveController")
@ExtendWith(MockitoExtension.class)
class ReactiveControllerTest {

    private TestController controller;

    @Mock
    private RoutingContext ctx;

    @Mock
    private HttpServerRequest request;

    @Mock
    private HttpServerResponse response;

    @Mock
    private RequestBody requestBody;

    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        controller = new TestController();
    }

    private void setupResponseMocks() {
        lenient().when(ctx.response()).thenReturn(response);
        lenient().when(response.ended()).thenReturn(false);
        lenient().when(response.setStatusCode(anyInt())).thenReturn(response);
        lenient().when(response.putHeader(anyString(), anyString())).thenReturn(response);
        lenient().when(response.end(any(String.class))).thenReturn(Future.succeededFuture());
        lenient().when(ctx.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME)).thenReturn("test-trace-id");
    }

    // ========== Path Parameters ==========

    @Nested
    @DisplayName("Path Parameters")
    class PathParams {

        @Test
        void getPathParam_existingParam_returnsValue() {
            when(ctx.pathParam("id")).thenReturn("123");
            assertEquals("123", controller.getPathParam(ctx, "id"));
        }

        @Test
        void getPathParam_missingParam_throwsInternalServiceException() {
            when(ctx.pathParam("id")).thenReturn(null);
            InternalServiceException ex = assertThrows(
                    InternalServiceException.class,
                    () -> controller.getPathParam(ctx, "id"));
            assertEquals(400, ex.getHttpCode());
            assertTrue(ex.getMessage().contains("id"));
        }

        @Test
        void getNonRequiredPathParam_existing_returnsValue() {
            when(ctx.pathParam("id")).thenReturn("abc");
            assertEquals("abc", controller.getNonRequiredPathParam(ctx, "id"));
        }

        @Test
        void getNonRequiredPathParam_missing_returnsNull() {
            when(ctx.pathParam("id")).thenReturn(null);
            assertNull(controller.getNonRequiredPathParam(ctx, "id"));
        }

        @Test
        void getPathParam_withFunction_convertsValue() {
            when(ctx.pathParam("id")).thenReturn("42");
            Integer result = controller.getPathParam(ctx, "id", Integer::parseInt);
            assertEquals(42, result);
        }

        @Test
        void getNonRequiredPathParam_withFunction_existingParam_convertsValue() {
            when(ctx.pathParam("id")).thenReturn("100");
            Long result = controller.getNonRequiredPathParam(ctx, "id", Long::parseLong);
            assertEquals(100L, result);
        }

        @Test
        void getNonRequiredPathParam_withFunction_missingParam_appliesNullToFunction() {
            when(ctx.pathParam("id")).thenReturn(null);
            // Function receives null when param is missing
            String result = controller.getNonRequiredPathParam(ctx, "id", v -> v == null ? "default" : v);
            assertEquals("default", result);
        }
    }

    // ========== Query Parameters ==========

    @Nested
    @DisplayName("Query Parameters")
    class QueryParams {

        @Test
        void getRequestQueryParam_existingParam_returnsValue() {
            when(ctx.queryParam("name")).thenReturn(List.of("John"));
            assertEquals("John", controller.getRequestQueryParam(ctx, "name"));
        }

        @Test
        void getRequestQueryParam_missingParam_throwsInternalServiceException() {
            when(ctx.queryParam("name")).thenReturn(Collections.emptyList());
            InternalServiceException ex = assertThrows(
                    InternalServiceException.class,
                    () -> controller.getRequestQueryParam(ctx, "name"));
            assertEquals(400, ex.getHttpCode());
        }

        @Test
        void getRequestQueryParam_blankValue_throwsInternalServiceException() {
            when(ctx.queryParam("name")).thenReturn(List.of("  "));
            assertThrows(InternalServiceException.class,
                    () -> controller.getRequestQueryParam(ctx, "name"));
        }

        @Test
        void getRequestQueryParam_withFunction_convertsValue() {
            when(ctx.queryParam("age")).thenReturn(List.of("25"));
            Integer result = controller.getRequestQueryParam(ctx, "age", Integer::parseInt);
            assertEquals(25, result);
        }

        @Test
        void getRequestQueryParamInList_commaSeparated_returnsList() {
            when(ctx.queryParam("ids")).thenReturn(List.of("1,2,3"));
            List<String> result = controller.getRequestQueryParamInList(ctx, "ids");
            assertEquals(3, result.size());
            assertEquals("1", result.get(0));
            assertEquals("2", result.get(1));
            assertEquals("3", result.get(2));
        }

        @Test
        void getRequestQueryParamInList_withFunction_convertsList() {
            when(ctx.queryParam("ids")).thenReturn(List.of("10,20,30"));
            List<Integer> result = controller.getRequestQueryParamInList(ctx, "ids", Integer::parseInt);
            assertEquals(List.of(10, 20, 30), result);
        }

        @Test
        void getRequestQueryParamInList_emptyList_throwsException() {
            when(ctx.queryParam("ids")).thenReturn(Collections.emptyList());
            assertThrows(InternalServiceException.class,
                    () -> controller.getRequestQueryParamInList(ctx, "ids"));
        }

        @Test
        void getNoneRequiringRequestQueryParam_existing_returnsValue() {
            when(ctx.queryParam("filter")).thenReturn(List.of("active"));
            assertEquals("active", controller.getNoneRequiringRequestQueryParam(ctx, "filter"));
        }

        @Test
        void getNoneRequiringRequestQueryParam_missing_returnsNull() {
            when(ctx.queryParam("filter")).thenReturn(Collections.emptyList());
            assertNull(controller.getNoneRequiringRequestQueryParam(ctx, "filter"));
        }

        @Test
        void getNoneRequiringRequestQueryParam_blankValue_returnsNull() {
            when(ctx.queryParam("filter")).thenReturn(List.of("  "));
            assertNull(controller.getNoneRequiringRequestQueryParam(ctx, "filter"));
        }

        @Test
        void getNoneRequiringRequestQueryParam_withFunction_existing_convertsValue() {
            when(ctx.queryParam("page")).thenReturn(List.of("5"));
            Integer result = controller.getNoneRequiringRequestQueryParam(ctx, "page", Integer::parseInt);
            assertEquals(5, result);
        }

        @Test
        void getNoneRequiringRequestQueryParam_withFunction_missing_returnsNull() {
            when(ctx.queryParam("page")).thenReturn(Collections.emptyList());
            assertNull(controller.getNoneRequiringRequestQueryParam(ctx, "page", Integer::parseInt));
        }

        @Test
        void getNoneRequiringRequestQueryParam_withFunction_blankValue_returnsNull() {
            when(ctx.queryParam("page")).thenReturn(List.of(""));
            assertNull(controller.getNoneRequiringRequestQueryParam(ctx, "page", Integer::parseInt));
        }

        @Test
        void getNoneRequiringRequestQueryParamInList_existing_returnsList() {
            when(ctx.queryParam("tags")).thenReturn(List.of("a,b,c"));
            List<String> result = controller.getNoneRequiringRequestQueryParamInList(ctx, "tags");
            assertEquals(3, result.size());
        }

        @Test
        void getNoneRequiringRequestQueryParamInList_missing_returnsEmptyList() {
            when(ctx.queryParam("tags")).thenReturn(Collections.emptyList());
            List<String> result = controller.getNoneRequiringRequestQueryParamInList(ctx, "tags");
            assertTrue(result.isEmpty());
        }

        @Test
        void getNoneRequiringRequestQueryParamInList_withFunction_convertsList() {
            when(ctx.queryParam("nums")).thenReturn(List.of("1,2,3"));
            List<Integer> result = controller.getNoneRequiringRequestQueryParamInList(ctx, "nums", Integer::parseInt);
            assertEquals(List.of(1, 2, 3), result);
        }
    }

    // ========== Header Parameters ==========

    @Nested
    @DisplayName("Header Parameters")
    class HeaderParams {

        @Test
        void getRequestHeaderParam_existingHeader_returnsValue() {
            when(ctx.request()).thenReturn(request);
            when(request.getHeader("Authorization")).thenReturn("Bearer token123");
            assertEquals("Bearer token123", controller.getRequestHeaderParam(ctx, "Authorization"));
        }

        @Test
        void getRequestHeaderParam_missingHeader_throwsInternalServiceException() {
            when(ctx.request()).thenReturn(request);
            when(request.getHeader("Authorization")).thenReturn(null);
            InternalServiceException ex = assertThrows(
                    InternalServiceException.class,
                    () -> controller.getRequestHeaderParam(ctx, "Authorization"));
            assertEquals(400, ex.getHttpCode());
        }

        @Test
        void getRequestHeaderParam_blankHeader_throwsInternalServiceException() {
            when(ctx.request()).thenReturn(request);
            when(request.getHeader("Authorization")).thenReturn("  ");
            assertThrows(InternalServiceException.class,
                    () -> controller.getRequestHeaderParam(ctx, "Authorization"));
        }

        @Test
        void getNoneRequiringRequestHeaderParam_existing_returnsValue() {
            when(ctx.request()).thenReturn(request);
            when(request.getHeader("X-Custom")).thenReturn("value");
            assertEquals("value", controller.getNoneRequiringRequestHeaderParam(ctx, "X-Custom"));
        }

        @Test
        void getNoneRequiringRequestHeaderParam_missing_returnsNull() {
            when(ctx.request()).thenReturn(request);
            when(request.getHeader("X-Custom")).thenReturn(null);
            assertNull(controller.getNoneRequiringRequestHeaderParam(ctx, "X-Custom"));
        }
    }

    // ========== Form and File Parameters ==========

    @Nested
    @DisplayName("Form and File Parameters")
    class FormFileParams {

        @Test
        void getFormParam_returnsFormAttribute() {
            when(ctx.request()).thenReturn(request);
            when(request.getFormAttribute("username")).thenReturn("admin");
            assertEquals("admin", controller.getFormParam(ctx, "username"));
        }

        @Test
        void getFileParam_existingFile_returnsFileUpload() {
            FileUpload upload1 = mock(FileUpload.class);
            FileUpload upload2 = mock(FileUpload.class);
            when(upload1.name()).thenReturn("avatar");
            when(ctx.fileUploads()).thenReturn(List.of(upload1, upload2));
            FileUpload result = controller.getFileParam(ctx, "avatar");
            assertSame(upload1, result);
        }

        @Test
        void getFileParam_noMatch_returnsNull() {
            FileUpload upload1 = mock(FileUpload.class);
            when(upload1.name()).thenReturn("other");
            when(ctx.fileUploads()).thenReturn(List.of(upload1));
            assertNull(controller.getFileParam(ctx, "avatar"));
        }

        @Test
        void getFileParam_emptyUploads_returnsNull() {
            when(ctx.fileUploads()).thenReturn(List.of());
            assertNull(controller.getFileParam(ctx, "file"));
        }
    }

    // ========== Handle Request ==========

    @Nested
    @DisplayName("Handle Request")
    class HandleRequest {

        @Test
        void handleRequest_voidType_returnsNull() {
            assertNull(controller.handleRequest(ctx, gson, ReactiveController.VOID));
        }

        @Test
        void handleRequest_withGson_deserializesJson() {
            when(ctx.body()).thenReturn(requestBody);
            when(requestBody.asString(CommonConstant.UTF_8_STANDARD_CHARSET))
                    .thenReturn("{\"name\":\"test\",\"value\":42}");
            TypeToken<TestDto> type = new TypeToken<>() {};
            TestDto result = controller.handleRequest(ctx, gson, type);
            assertNotNull(result);
            assertEquals("test", result.name);
            assertEquals(42, result.value);
        }

        @Test
        void handleRequest_withObjectMapper_deserializesJson() {
            when(ctx.body()).thenReturn(requestBody);
            when(requestBody.asString(CommonConstant.UTF_8_STANDARD_CHARSET))
                    .thenReturn("{\"name\":\"test\",\"value\":42}");
            TypeToken<TestDto> type = new TypeToken<>() {};
            TestDto result = controller.handleRequest(ctx, objectMapper, type);
            assertNotNull(result);
            assertEquals("test", result.name);
            assertEquals(42, result.value);
        }

        @Test
        void handleRequest_withObjectMapper_invalidJson_throwsInternalServiceException() {
            when(ctx.body()).thenReturn(requestBody);
            when(requestBody.asString(CommonConstant.UTF_8_STANDARD_CHARSET))
                    .thenReturn("{invalid json");
            TypeToken<TestDto> type = new TypeToken<>() {};
            assertThrows(InternalServiceException.class,
                    () -> controller.handleRequest(ctx, objectMapper, type));
        }

        @Test
        void handleRequest_unknownJsonHandler_throwsInternalServiceException() {
            when(ctx.body()).thenReturn(requestBody);
            when(requestBody.asString(CommonConstant.UTF_8_STANDARD_CHARSET))
                    .thenReturn("{\"name\":\"test\"}");
            TypeToken<TestDto> type = new TypeToken<>() {};
            InternalServiceException ex = assertThrows(
                    InternalServiceException.class,
                    () -> controller.handleRequest(ctx, "not-a-handler", type));
            assertTrue(ex.getMessage().contains("Unknown json handler"));
        }

        @Test
        void handleRequest_commonRequest_callsValidate() {
            when(ctx.body()).thenReturn(requestBody);
            when(requestBody.asString(CommonConstant.UTF_8_STANDARD_CHARSET))
                    .thenReturn("{\"data\":\"valid\"}");
            TypeToken<TestCommonRequest> type = new TypeToken<>() {};
            TestCommonRequest result = controller.handleRequest(ctx, gson, type);
            assertNotNull(result);
            assertTrue(result.validated);
        }

        @Test
        void handleRequest_commonRequest_validationFails_throwsException() {
            when(ctx.body()).thenReturn(requestBody);
            when(requestBody.asString(CommonConstant.UTF_8_STANDARD_CHARSET))
                    .thenReturn("{\"data\":null}");
            TypeToken<TestFailingCommonRequest> type = new TypeToken<>() {};
            assertThrows(InternalServiceException.class,
                    () -> controller.handleRequest(ctx, gson, type));
        }
    }

    // ========== Handle Response ==========

    @Nested
    @DisplayName("Handle Response")
    class HandleResponse {

        @BeforeEach
        void setUpResponse() {
            setupResponseMocks();
        }

        @Test
        void handleResponse_setsStatus200AndBody() {
            controller.handleResponse(ctx, gson, "hello");
            verify(response).setStatusCode(200);
            verify(response).end((String) argThat(s -> "hello".equals(s)));
        }

        @Test
        void handleResponse_objectBody_serializesToJson() {
            TestDto dto = new TestDto();
            dto.name = "test";
            dto.value = 42;
            controller.handleResponse(ctx, gson, dto);
            verify(response).setStatusCode(200);
            verify(response).end((String) argThat(s -> ((String) s).contains("test")));
        }

        @Test
        void handleResponse_withObjectMapper_serializesToJson() {
            TestDto dto = new TestDto();
            dto.name = "jackson";
            dto.value = 99;
            controller.handleResponse(ctx, objectMapper, dto);
            verify(response).setStatusCode(200);
            verify(response).end((String) argThat(body -> ((String) body).contains("jackson")));
        }

        @Test
        void handleResponse_responseEntity_overridesStatus() {
            ResponseEntity<String> entity = ResponseEntity.of(201, "created");
            controller.handleResponse(ctx, gson, entity);
            verify(response).setStatusCode(201);
            verify(response).end((String) argThat(s -> "created".equals(s)));
        }

        @Test
        void handleResponse_commonResponse_setsSuccessCodeAndTrace() {
            CommonResponse commonResp = new CommonResponse();
            controller.handleResponse(ctx, gson, commonResp);
            assertEquals("test-trace-id", commonResp.getTrace());
            assertEquals(ErrorCodeEnums.SUCCESS.getCode(), commonResp.getErrorCode());
            assertEquals(ErrorCodeEnums.SUCCESS.getMessage(), commonResp.getErrorDescription());
        }

        @Test
        void handleResponse_responseEnded_doesNotWrite() {
            when(response.ended()).thenReturn(true);
            controller.handleResponse(ctx, gson, "test");
            verify(response, never()).setStatusCode(anyInt());
            verify(response, never()).end(any(String.class));
        }

        @Test
        void handleResponse_nullBody_serializesNull() {
            controller.handleResponse(ctx, gson, null, 200);
            verify(response).setStatusCode(200);
        }

        @Test
        void handleResponse_unknownJsonHandler_returnsErrorJson() {
            controller.handleResponse(ctx, "not-handler", new TestDto());
            verify(response).end((String) argThat(body -> ((String) body).contains("Unknown json handler")));
        }

        @Test
        void handleResponse_setsContentTypeHeader() {
            controller.handleResponse(ctx, gson, "ok");
            verify(response).putHeader(
                    VertxBaseConstant.CONTENT_TYPE_HEADER_NAME,
                    VertxBaseConstant.CONTENT_TYPE_APPLICATION_JSON);
        }

        @Test
        void handleResponse_setsTraceHeader() {
            controller.handleResponse(ctx, gson, "ok");
            verify(response).putHeader(VertxBaseConstant.TRACE_HEADER_NAME, "test-trace-id");
        }

        @Test
        void handleResponse_setsProcessedTimeHeader() {
            controller.handleResponse(ctx, gson, "ok");
            verify(response).putHeader(eq(VertxBaseConstant.PROCESSED_TIME_HEADER_NAME), anyString());
        }

        @Test
        void handleResponse_responseEntity_withCommonResponse_setsTraceAndStatus() {
            CommonResponse body = new CommonResponse();
            ResponseEntity<CommonResponse> entity = ResponseEntity.of(202, body);
            controller.handleResponse(ctx, gson, entity);
            verify(response).setStatusCode(202);
            assertEquals("test-trace-id", body.getTrace());
            assertEquals(202, body.getHttpCode());
        }
    }

    // ========== Handle Error ==========

    @Nested
    @DisplayName("Handle Error")
    class HandleError {

        @BeforeEach
        void setUpResponse() {
            setupResponseMocks();
        }

        @Test
        void handleError_internalServiceException_mapsCorrectly() {
            InternalServiceException ex = new InternalServiceException(ErrorCodeEnums.INVALID_REQUEST, "bad input");
            controller.handleError(ctx, gson, ex);
            verify(response).setStatusCode(400);
        }

        @Test
        void handleError_wrappedInternalServiceException_mapsCorrectly() {
            InternalServiceException inner = new InternalServiceException(ErrorCodeEnums.DATA_NOT_FOUND, "not found");
            RuntimeException wrapper = new RuntimeException("wrapped", inner);
            controller.handleError(ctx, gson, wrapper);
            verify(response).setStatusCode(404);
        }

        @Test
        void handleError_genericException_returns500() {
            RuntimeException ex = new RuntimeException("unexpected error");
            controller.handleError(ctx, gson, ex);
            verify(response).setStatusCode(500);
        }

        @Test
        void handleError_setsTraceId() {
            RuntimeException ex = new RuntimeException("error");
            controller.handleError(ctx, gson, ex);
            verify(response).end((String) argThat(body -> ((String) body).contains("test-trace-id")));
        }

        @Test
        void handleError_responseEnded_doesNotWrite() {
            when(response.ended()).thenReturn(true);
            controller.handleError(ctx, gson, new RuntimeException("err"));
            verify(response, never()).setStatusCode(anyInt());
        }

        @Test
        void handleError_internalServiceException_setsErrorCodeInBody() {
            InternalServiceException ex = new InternalServiceException(ErrorCodeEnums.INTERNAL_ERROR, "fail");
            controller.handleError(ctx, gson, ex);
            verify(response).end((String) argThat(body ->
                    ((String) body).contains(String.valueOf(ErrorCodeEnums.INTERNAL_ERROR.getCode()))));
        }

        @Test
        void handleError_genericException_setsErrorCodeMinus1() {
            controller.handleError(ctx, gson, new RuntimeException("fail"));
            verify(response).end((String) argThat(body -> ((String) body).contains("-1")));
        }
    }

    // ========== VOID TypeToken ==========

    @Nested
    @DisplayName("VOID constant")
    class VoidConstant {

        @Test
        void VOID_isTypeTokenOfVoid() {
            assertNotNull(ReactiveController.VOID);
            assertEquals(Void.class, ReactiveController.VOID.getRawType());
        }
    }

    // ========== Test helpers ==========

    static class TestController extends ReactiveController {
    }

    public static class TestDto {
        public String name;
        public int value;
    }

    public static class TestCommonRequest implements CommonRequest {
        public String data;
        public transient boolean validated = false;

        @Override
        public void validate() {
            validated = true;
        }
    }

    public static class TestFailingCommonRequest implements CommonRequest {
        public String data;

        @Override
        public void validate() {
            throw new RuntimeException("Validation failed: data is required");
        }
    }
}
