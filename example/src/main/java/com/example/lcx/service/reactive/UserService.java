package com.example.lcx.service.reactive;

import com.example.lcx.entity.reactive.UserEntity;
import com.example.lcx.entity.reactive.UserEntityUtils;
import com.example.lcx.enums.AppError;
import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.example.lcx.object.response.UserLoginResponse;
import com.example.lcx.respository.reactive.UserRepository;
import com.google.gson.Gson;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.BCryptUtils;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.constant.VertxBaseConstant;
import vn.com.lcx.vertx.base.exception.InternalServiceException;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserService {
    private final Pool pool;
    private final UserRepository userRepository;
    private final Gson gson;
    private final JWTAuth jwtAuth;

    public Future<Void> createNew(RoutingContext context, final CreateNewUserRequest request) {
        Promise<Void> promise = Promise.promise();
        final List<UserEntity> users = new ArrayList<>();
        pool.getConnection().compose(conn ->
                conn.begin().compose(tx ->
                        userRepository.findByUsername(conn, request.getUsername())
                                .compose(rowSet -> {
                                    for (Row row : rowSet) {
                                        users.add(UserEntityUtils.vertxRowMapping(row));
                                    }
                                    if (!users.isEmpty()) {
                                        return Future.failedFuture(new InternalServiceException(AppError.USER_EXISTED));
                                    } else {
                                        UserEntity user = new UserEntity();
                                        user.setUsername(request.getUsername());
                                        user.setPassword(BCryptUtils.hashPassword(request.getPassword()));
                                        user.setFullName(request.getFullName());
                                        return userRepository.save(conn, user);
                                    }
                                })
                                .compose(rowSet -> {
                                    // Optionally extract ID or do something with the saved user, if needed
                                    // com.example.lcx.entity.reactive.UserEntityUtils.idRowExtract(...);
                                    return tx.commit();
                                })
                                .onSuccess(v -> {
                                    CommonResponse response = CommonResponse.builder()
                                            .trace(context.get(CommonConstant.TRACE_ID_MDC_KEY_NAME))
                                            .build();
                                    context.response()
                                            .putHeader(VertxBaseConstant.CONTENT_TYPE_HEADER_NAME, VertxBaseConstant.CONTENT_TYPE_APPLICATION_JSON)
                                            .putHeader(
                                                    VertxBaseConstant.PROCESSED_TIME_HEADER_NAME,
                                                    DateTimeUtils.generateCurrentTimeDefault()
                                                            .format(
                                                                    DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN)
                                                            )
                                            )
                                            .putHeader(VertxBaseConstant.TRACE_HEADER_NAME, context.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
                                    context.end(gson.toJson(response));
                                    promise.complete();
                                })
                                .onFailure(err -> {
                                    LogUtils.writeLog(context, err.getMessage(), err);
                                    CommonResponse response = CommonResponse.builder()
                                            .trace(context.get(CommonConstant.TRACE_ID_MDC_KEY_NAME))
                                            .errorCode(-1)
                                            .errorDescription(err.getMessage())
                                            .build();
                                    context.response().setStatusCode(500)
                                            .putHeader(VertxBaseConstant.CONTENT_TYPE_HEADER_NAME, VertxBaseConstant.CONTENT_TYPE_APPLICATION_JSON)
                                            .putHeader(
                                                    VertxBaseConstant.PROCESSED_TIME_HEADER_NAME,
                                                    DateTimeUtils.generateCurrentTimeDefault()
                                                            .format(
                                                                    DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN)
                                                            )
                                            )
                                            .putHeader(VertxBaseConstant.TRACE_HEADER_NAME, context.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
                                    context.end(gson.toJson(response));
                                    tx.rollback();
                                })
                                .eventually(() -> {
                                    conn.close();
                                    return Future.succeededFuture();
                                })
                )
        ).onFailure(promise::fail);
        return promise.future();
    }

    public Future<Void> login(RoutingContext context, final UserLoginRequest request) {
        Promise<Void> promise = Promise.promise();
        final List<UserEntity> users = new ArrayList<>();
        pool.getConnection().compose(conn ->
                userRepository.findByUsername(conn, request.getUsername())
                        .compose(rowSet -> {
                            for (Row row : rowSet) {
                                users.add(UserEntityUtils.vertxRowMapping(row));
                            }
                            if (users.isEmpty()) {
                                return Future.failedFuture(new InternalServiceException(AppError.USER_NOT_EXIST));
                            }
                            UserEntity user = users.get(0);
                            try {
                                BCryptUtils.comparePassword(request.getPassword(), user.getPassword());
                            } catch (Exception e) {
                                return Future.failedFuture(new InternalServiceException(AppError.INCORRECT_PASSWORD));
                            }
                            return Future.succeededFuture(user);
                        })
                        .onSuccess(user -> {
                            UserLoginResponse response = new UserLoginResponse();
                            response.setTrace(context.get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
                            context.response()
                                    .putHeader(VertxBaseConstant.CONTENT_TYPE_HEADER_NAME, VertxBaseConstant.CONTENT_TYPE_APPLICATION_JSON)
                                    .putHeader(
                                            VertxBaseConstant.PROCESSED_TIME_HEADER_NAME,
                                            DateTimeUtils.generateCurrentTimeDefault()
                                                    .format(
                                                            DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN)
                                                    )
                                    )
                                    .putHeader(VertxBaseConstant.TRACE_HEADER_NAME, context.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
                            context.end(gson.toJson(response));
                            promise.complete();
                        })
                        .onFailure(err -> {
                            LogUtils.writeLog(context, err.getMessage(), err);
                            CommonResponse response = CommonResponse.builder()
                                    .trace(context.get(CommonConstant.TRACE_ID_MDC_KEY_NAME))
                                    .errorCode(-1)
                                    .errorDescription(err.getMessage())
                                    .build();
                            context.response().setStatusCode(401)
                                    .putHeader(VertxBaseConstant.CONTENT_TYPE_HEADER_NAME, VertxBaseConstant.CONTENT_TYPE_APPLICATION_JSON)
                                    .putHeader(
                                            VertxBaseConstant.PROCESSED_TIME_HEADER_NAME,
                                            DateTimeUtils.generateCurrentTimeDefault()
                                                    .format(
                                                            DateTimeFormatter.ofPattern(CommonConstant.DEFAULT_LOCAL_DATE_TIME_STRING_PATTERN)
                                                    )
                                    )
                                    .putHeader(VertxBaseConstant.TRACE_HEADER_NAME, context.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME));
                            context.end(gson.toJson(response));
                        })
                        .eventually(conn::close)
        ).onFailure(promise::fail);
        return promise.future();
    }
}
