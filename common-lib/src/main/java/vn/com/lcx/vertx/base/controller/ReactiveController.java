package vn.com.lcx.vertx.base.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.MyStringUtils;
import vn.com.lcx.vertx.base.constant.VertxBaseConstant;
import vn.com.lcx.vertx.base.enums.ErrorCodeEnums;
import vn.com.lcx.vertx.base.exception.InternalServiceException;
import vn.com.lcx.vertx.base.http.response.CommonResponse;
import vn.com.lcx.vertx.base.validate.AutoValidation;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ReactiveController {

    public final static TypeToken<Void> VOID = new TypeToken<Void>() {
    };

    public String getRequestQueryParam(RoutingContext context, String paramName) {
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
        return paramValue.get(0);
    }

    public <T> T getRequestQueryParam(RoutingContext context, String paramName, Function<String, T> function) {
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
        return function.apply(paramValue.get(0));
    }

    public List<String> getRequestQueryParamInList(RoutingContext context, String paramName) {
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

    public <T> List<T> getRequestQueryParamInList(RoutingContext context, String paramName, Function<String, T> function) {
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
        return paramValue.stream().map(function).collect(Collectors.toList());
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
        final var paramValue = context.queryParam(paramName).isEmpty() ?
                new ArrayList<String>() :
                Arrays.stream(context.queryParam(paramName).get(0).split(",")).map(String::trim).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(paramValue)) {
            new ArrayList<>();
        }
        return paramValue;
    }

    public <T> List<T> getNoneRequiringRequestQueryParamInList(RoutingContext context, String paramName, Function<String, T> function) {
        final var paramValue = context.queryParam(paramName).isEmpty() ?
                new ArrayList<String>() :
                Arrays.stream(context.queryParam(paramName).get(0).split(",")).map(String::trim).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(paramValue)) {
            new ArrayList<>();
        }
        return paramValue.stream().map(function).collect(Collectors.toList());
    }

    public void handleError(RoutingContext ctx, Gson gson, Throwable e) {
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
        String responseBody = gson.toJson(response);
        ctx.end(responseBody);
    }

    public <T extends CommonResponse> void handleResponse(RoutingContext ctx, Gson gson, T resp) {
        resp.setTrace(ctx.get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
        resp.setErrorCode(ErrorCodeEnums.SUCCESS.getCode());
        resp.setErrorDescription(ErrorCodeEnums.SUCCESS.getMessage());
        resp.setHttpCode(200);
        ctx.response().setStatusCode(200)
                .putHeader(VertxBaseConstant.CONTENT_TYPE_HEADER_NAME, VertxBaseConstant.CONTENT_TYPE_APPLICATION_JSON)
                .putHeader(
                        VertxBaseConstant.PROCESSED_TIME_HEADER_NAME,
                        DateTimeUtils.generateCurrentTimeDefault().format(
                                DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN)
                        )
                )
                .putHeader(VertxBaseConstant.TRACE_HEADER_NAME, ctx.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
        String responseBody = gson.toJson(resp);
        ctx.end(responseBody);
    }

    public <T> T handleRequest(RoutingContext ctx, Gson gson, TypeToken<T> reqType) {
        final var headerLogMsg = new ArrayList<String>();
        final MultiMap requestHeader = ctx.request().headers();

        for (Map.Entry<String, String> requestQueryParam : requestHeader) {
            headerLogMsg.add(
                    String.format(
                            "        - Name: %s\n          Value: %s",
                            requestQueryParam.getKey(),
                            requestQueryParam.getValue()
                    )
            );
        }
        LogUtils.writeLog(ctx, LogUtils.Level.INFO, "Header:\n{}", String.join("\n", headerLogMsg));
        LogUtils.writeLog(ctx, LogUtils.Level.INFO, "Url: {}", ctx.request().uri());
        if (VOID.equals(reqType)) {
            return null;
        }
        final var requestBody = MyStringUtils.minifyJsonString(ctx.body().asString(CommonConstant.UTF_8_STANDARD_CHARSET));
        T requestObject = gson.fromJson(requestBody, reqType);
        final var errorFields = AutoValidation.validate(requestObject);
        if (!errorFields.isEmpty()) {
            throw new InternalServiceException(ErrorCodeEnums.INVALID_REQUEST, errorFields.toString());
        }
        return requestObject;
    }

}
