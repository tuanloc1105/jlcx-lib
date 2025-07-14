package com.example.lcx.service;

import com.example.lcx.entity.UserEntity;
import com.example.lcx.enums.AppError;
import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.example.lcx.object.response.UserLoginResponse;
import com.example.lcx.respository.UserRepository;
import com.google.gson.Gson;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.utils.BCryptUtils;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

@Component
@RequiredArgsConstructor
public class UserService {
    private final Pool pool;
    private final UserRepository userRepository;
    private final Gson gson;
    private final JWTAuth jwtAuth;

    public Future<Void> createNew(final RoutingContext context, final CreateNewUserRequest request) {
        return pool.getConnection().compose(conn ->
                conn.begin().compose(tx ->
                        userRepository.findByUsername(context, conn, request.getUsername())
                                .compose(optionalUserEntity -> {
                                    if (optionalUserEntity.isPresent()) {
                                        return Future.failedFuture(new InternalServiceException(AppError.USER_EXISTED));
                                    } else {
                                        final var currentTime = DateTimeUtils.generateCurrentTimeDefault();
                                        UserEntity user = new UserEntity();
                                        user.setUsername(request.getUsername());
                                        user.setPassword(BCryptUtils.hashPassword(request.getPassword()));
                                        user.setFullName(request.getFullName());
                                        user.setCreatedAt(currentTime);
                                        user.setUpdatedAt(currentTime);
                                        return userRepository.save(context, conn, user);
                                    }
                                })
                                .compose(user -> {
                                    // Optionally extract ID or do something with the saved user, if needed
                                    // com.example.lcx.entity.reactive.UserEntityUtils.idRowExtract(...);
                                    return tx.commit();
                                })
                                .onFailure(err -> {
                                    tx.rollback().onComplete(rollbackResult -> {
                                        if (rollbackResult.failed()) {
                                            err.addSuppressed(rollbackResult.cause());
                                        }
                                    });
                                })
                                .eventually(conn::close)
                ).map(it -> null)
        );
    }

    public Future<UserLoginResponse> login(final RoutingContext context, final UserLoginRequest request) {
        return pool.getConnection().compose(conn ->
                userRepository.findByUsername(context, conn, request.getUsername())
                        .compose(optionalUserEntity -> {
                            if (optionalUserEntity.isEmpty()) {
                                return Future.failedFuture(new InternalServiceException(AppError.USER_NOT_EXIST));
                            }
                            UserEntity user = optionalUserEntity.get();
                            try {
                                BCryptUtils.comparePassword(request.getPassword(), user.getPassword());
                            } catch (Exception e) {
                                return Future.failedFuture(new InternalServiceException(AppError.INCORRECT_PASSWORD));
                            }
                            final var tokenInfo = UserJWTTokenInfo.builder()
                                    .id(user.getId().longValue())
                                    .username(user.getUsername())
                                    .fullName(user.getFullName())
                                    .build();
                            String token = jwtAuth.generateToken(
                                    new JsonObject(this.gson.toJson(tokenInfo)),
                                    new JWTOptions().setAlgorithm("RS256").setExpiresInMinutes(14400)
                            );
                            UserLoginResponse response = new UserLoginResponse(token, user.getFullName());
                            return Future.succeededFuture(response);
                        })
                        .eventually(conn::close)
        );
    }

    public Future<UserEntity> validateUser(final RoutingContext context, SqlConnection sqlConnection, final String username) {
        return userRepository.findByUsername(context, sqlConnection, username)
                .compose(optionalUserEntity -> {
                    if (optionalUserEntity.isEmpty()) {
                        return Future.failedFuture(new InternalServiceException(AppError.USER_NOT_EXIST));
                    }
                    final UserEntity user = optionalUserEntity.get();
                    if (user.getDeletedAt() != null) {
                        return Future.failedFuture(new InternalServiceException(AppError.INACTIVE_USER));
                    }
                    return Future.succeededFuture(user);
                });
    }

}
