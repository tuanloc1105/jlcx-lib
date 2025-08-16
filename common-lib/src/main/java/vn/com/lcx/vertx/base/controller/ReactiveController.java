package vn.com.lcx.vertx.base.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.Strictness;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.common.utils.ExceptionUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.constant.VertxBaseConstant;
import vn.com.lcx.vertx.base.enums.ErrorCodeEnums;
import vn.com.lcx.vertx.base.exception.InternalServiceException;
import vn.com.lcx.vertx.base.http.response.CommonResponse;
import vn.com.lcx.vertx.base.validate.AutoValidation;

import java.io.StringReader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ReactiveController {

    public final static TypeToken<Void> VOID = new TypeToken<>() {
    };

    public String getPathParam(RoutingContext context, String paramName) {
        final var val = context.pathParam(paramName);
        if (val == null) {
            throw new InternalServiceException(
                    ErrorCodeEnums.INVALID_REQUEST,
                    String.format("Path parameter `%s` can not be empty", paramName)
            );
        }
        return val;
    }

    public String getNonRequiredPathParam(RoutingContext context, String paramName) {
        try {
            return getPathParam(context, paramName);
        } catch (Exception e) {
            return CommonConstant.EMPTY_STRING;
        }
    }

    public <T> T getPathParam(RoutingContext context, String paramName, Function<String, T> function) {
        return function.apply(getPathParam(context, paramName));
    }

    public <T> T getNonRequiredPathParam(RoutingContext context, String paramName, Function<String, T> function) {
        return function.apply(getNonRequiredPathParam(context, paramName));
    }

    public String getRequestQueryParam(RoutingContext context, String paramName) {
        List<String> paramValue = extractQueryParam(context, paramName);
        return paramValue.get(0);
    }

    public <T> T getRequestQueryParam(RoutingContext context, String paramName, Function<String, T> function) {
        return function.apply(extractQueryParam(context, paramName).get(0));
    }

    private List<String> extractQueryParam(RoutingContext context, String paramName) {
        final var paramValue = context.queryParam(paramName);
        if (CollectionUtils.isEmpty(paramValue)) {
            throw new InternalServiceException(
                    ErrorCodeEnums.INVALID_REQUEST,
                    String.format("Query parameter `%s` can not be empty", paramName)
            );
        }
        if (StringUtils.isBlank(paramValue.get(0))) {
            throw new InternalServiceException(
                    ErrorCodeEnums.INVALID_REQUEST,
                    String.format("Query parameter `%s` can not be empty", paramName)
            );
        }
        return paramValue;
    }

    public List<String> getRequestQueryParamInList(RoutingContext context, String paramName) {
        return extractQueryParams(context, paramName);
    }

    public <T> List<T> getRequestQueryParamInList(RoutingContext context, String paramName, Function<String, T> function) {
        return extractQueryParams(context, paramName).stream().map(function).collect(Collectors.toList());
    }

    private List<String> extractQueryParams(RoutingContext context, String paramName) {
        final List<String> paramValue = context.queryParam(paramName).isEmpty() ?
                new ArrayList<>() :
                Arrays.stream(context.queryParam(paramName).get(0).split(",")).map(String::trim).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(paramValue)) {
            throw new InternalServiceException(
                    ErrorCodeEnums.INVALID_REQUEST,
                    String.format("%s can not be empty", paramValue)
            );
        }
        if (paramValue.stream().anyMatch(StringUtils::isBlank)) {
            throw new InternalServiceException(
                    ErrorCodeEnums.INVALID_REQUEST,
                    String.format("%s's elements can not be empty", paramValue)
            );
        }
        return paramValue;
    }

    public String getNoneRequiringRequestQueryParam(RoutingContext context, String paramName) {
        final var paramValue = context.queryParam(paramName);
        if (CollectionUtils.isEmpty(paramValue)) {
            return CommonConstant.EMPTY_STRING;
        }
        return paramValue.get(0);
    }

    public <T> T getNoneRequiringRequestQueryParam(RoutingContext context, String paramName, Function<String, T> function) {
        final var paramValue = context.queryParam(paramName);
        if (CollectionUtils.isEmpty(paramValue)) {
            return null;
        }
        return function.apply(paramValue.get(0));
    }

    public List<String> getNoneRequiringRequestQueryParamInList(RoutingContext context, String paramName) {
        return extractNonRequiredQueryParams(context, paramName);
    }

    public <T> List<T> getNoneRequiringRequestQueryParamInList(RoutingContext context, String paramName, Function<String, T> function) {
        return extractNonRequiredQueryParams(context, paramName).stream().map(function).collect(Collectors.toList());
    }

    private List<String> extractNonRequiredQueryParams(RoutingContext context, String paramName) {
        final var paramValue = context.queryParam(paramName).isEmpty() ?
                new ArrayList<String>() :
                Arrays.stream(context.queryParam(paramName).get(0).split(",")).map(String::trim).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(paramValue)) {
            new ArrayList<>();
        }
        return paramValue;
    }

    public void handleError(RoutingContext ctx, Object jsonHandler, Throwable e) {
        LogUtils.writeLog(ctx, e.getMessage(), e);
        CommonResponse response;
        int httpCode = 500;
        if (e instanceof InternalServiceException) {
            InternalServiceException internalServiceException = (InternalServiceException) e;
            httpCode = internalServiceException.getHttpCode();
            response = CommonResponse.builder()
                    .trace(ctx.get(CommonConstant.TRACE_ID_MDC_KEY_NAME))
                    .errorCode(internalServiceException.getCode())
                    .errorDescription(internalServiceException.getMessage())
                    .httpCode(httpCode)
                    .build();
        } else {
            response = CommonResponse.builder()
                    .trace(ctx.get(CommonConstant.TRACE_ID_MDC_KEY_NAME))
                    .errorCode(-1)
                    .errorDescription(e.getMessage())
                    .httpCode(httpCode)
                    .build();
        }
        ctx.response().setStatusCode(httpCode)
                .putHeader(VertxBaseConstant.CONTENT_TYPE_HEADER_NAME, VertxBaseConstant.CONTENT_TYPE_APPLICATION_JSON)
                .putHeader(
                        VertxBaseConstant.PROCESSED_TIME_HEADER_NAME,
                        DateTimeUtils.generateCurrentTimeDefault().format(
                                DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN)
                        )
                )
                .putHeader(VertxBaseConstant.TRACE_HEADER_NAME, ctx.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
        String responseBody;
        if (jsonHandler instanceof Gson) {
            responseBody = ((Gson) jsonHandler).toJson(response);
        } else if (jsonHandler instanceof ObjectMapper) {
            try {
                responseBody = ((ObjectMapper) jsonHandler).writeValueAsString(response);
            } catch (JsonProcessingException jsonProcessingException) {
                responseBody = String.format(
                        "{\n" +
                                "    \"error\": \"%s\"\n" +
                                "}",
                        ExceptionUtils.getStackTrace(jsonProcessingException)
                );
            }
        } else {
            responseBody = "{\n    \"error\": \"Unknown json handler. Only support Gson and Jackson\"\n}";
        }
        ctx.end(responseBody);
    }

    public void handleResponse(RoutingContext ctx, Object jsonHandler, Object resp) {
        handleResponse(ctx, jsonHandler, resp, 200);
    }

    public void handleResponse(RoutingContext ctx, Object jsonHandler, Object resp, int code) {
        if (resp instanceof CommonResponse) {
            ((CommonResponse) resp).setTrace(ctx.get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
            ((CommonResponse) resp).setErrorCode(ErrorCodeEnums.SUCCESS.getCode());
            ((CommonResponse) resp).setErrorDescription(ErrorCodeEnums.SUCCESS.getMessage());
            ((CommonResponse) resp).setHttpCode(code);
        }
        ctx.response().setStatusCode(code)
                .putHeader(VertxBaseConstant.CONTENT_TYPE_HEADER_NAME, VertxBaseConstant.CONTENT_TYPE_APPLICATION_JSON)
                .putHeader(
                        VertxBaseConstant.PROCESSED_TIME_HEADER_NAME,
                        DateTimeUtils.generateCurrentTimeDefault().format(
                                DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN)
                        )
                )
                .putHeader(VertxBaseConstant.TRACE_HEADER_NAME, ctx.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
        String responseBody;
        if (jsonHandler instanceof Gson) {
            responseBody = ((Gson) jsonHandler).toJson(resp);
        } else if (jsonHandler instanceof ObjectMapper) {
            try {
                responseBody = ((ObjectMapper) jsonHandler).writeValueAsString(resp);
            } catch (JsonProcessingException e) {
                responseBody = String.format(
                        "{\n" +
                                "    \"error\": \"%s\"\n" +
                                "}",
                        ExceptionUtils.getStackTrace(e)
                );
            }
        } else {
            responseBody = "{\n    \"error\": \"Unknown json handler. Only support Gson and Jackson\"\n}";
        }
        ctx.end(responseBody);
    }

    public <T> T handleRequest(RoutingContext ctx, Object jsonHandler, TypeToken<T> reqType) {
        if (VOID.equals(reqType)) {
            return null;
        }
        final var requestBody = ctx.body().asString(CommonConstant.UTF_8_STANDARD_CHARSET);
        JsonReader jsonReader = new JsonReader(new StringReader(requestBody));
        jsonReader.setStrictness(Strictness.LENIENT);
        T requestObject;
        if (jsonHandler instanceof Gson) {
            requestObject = ((Gson) jsonHandler).fromJson(jsonReader, reqType);
        } else if (jsonHandler instanceof ObjectMapper) {
            try {
                final var objectMapper = ((ObjectMapper) jsonHandler);
                JavaType jt = objectMapper.getTypeFactory().constructType(reqType.getType());
                requestObject = ((ObjectMapper) jsonHandler).readValue(requestBody, jt);
            } catch (JsonProcessingException e) {
                throw new InternalServiceException(ErrorCodeEnums.INTERNAL_ERROR, e.getMessage());
            }
        } else {
            throw new InternalServiceException(ErrorCodeEnums.INTERNAL_ERROR, "Unknown json handler. Only support Gson and Jackson");
        }
        final var errorFields = AutoValidation.validate(requestObject);
        if (!errorFields.isEmpty()) {
            throw new InternalServiceException(ErrorCodeEnums.INVALID_REQUEST, errorFields.toString());
        }
        return requestObject;
    }

}
