package com.example.service;

import com.example.mapper.UsersMapper;
import com.example.model.dto.UserJWTTokenInfo;
import com.example.model.entity.UsersEntity;
import com.example.model.enums.AppError;
import com.example.model.http.request.CreateNewUserRequest;
import com.example.model.http.request.UserLoginRequest;
import com.example.model.http.response.UserLoginResponse;
import com.example.repository.UsersRepository;
import com.google.gson.Gson;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.hibernate.reactive.stage.Stage;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.BCryptUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.jpa.respository.CriteriaHandler;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UsersService {

    private final Stage.SessionFactory sessionFactory;
    private final UsersRepository usersRepository;
    private final UsersMapper usersMapper;
    private final Gson gson;
    private final JWTAuth jwtAuth;

    public Future<Void> createNew(final RoutingContext context, final CreateNewUserRequest request) {
        final var completionStage = sessionFactory.withTransaction((session, transaction) ->
                {
                    CriteriaHandler<UsersEntity> usersEntityCriteriaHandler = (cb, cq, root) -> {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(
                                cb.equal(root.get("username"), request.getUsername())
                        );
                        return cb.and(predicates.toArray(Predicate[]::new));
                    };
                    final var future =  usersRepository.findOne(session, usersEntityCriteriaHandler)
                            .map(opt ->
                                    {
                                        if (opt.isPresent()) {
                                            throw new InternalServiceException(AppError.USER_EXISTED);
                                        }
                                        return CommonConstant.VOID;
                                    }
                            ).compose(v ->
                                    {
                                        UsersEntity user = new UsersEntity();
                                        user.setUsername(request.getUsername());
                                        user.setPassword(BCryptUtils.hashPassword(request.getPassword()));
                                        user.setFullName(request.getFullName());
                                        return usersRepository.save(session, user);
                                    }
                            ).map(CommonConstant.VOID)
                            .onFailure(e ->
                                    {
                                        LogUtils.writeLog(context, e.getMessage(), e);
                                        transaction.markForRollback();
                                    }
                            );
                    return future.toCompletionStage();
                }
        );
        return Future.fromCompletionStage(completionStage);
    }

    public Future<UserLoginResponse> login(final RoutingContext context, final UserLoginRequest request) {
        final var completionStage = sessionFactory.withSession(session ->
                {
                    CriteriaHandler<UsersEntity> usersEntityCriteriaHandler = (cb, cq, root) -> {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(
                                cb.equal(root.get("username"), request.getUsername())
                        );
                        predicates.add(
                                cb.isNull(root.get("deletedAt"))
                        );
                        return cb.and(predicates.toArray(Predicate[]::new));
                    };
                    final var future = usersRepository.findOne(session, usersEntityCriteriaHandler)
                            .map(opt ->
                                    {
                                        if (opt.isEmpty()) {
                                            throw new InternalServiceException(AppError.USER_NOT_EXIST);
                                        }
                                        final var user = opt.get();
                                        try {
                                            BCryptUtils.comparePassword(request.getPassword(), user.getPassword());
                                        } catch (Exception e) {
                                            throw new InternalServiceException(AppError.INCORRECT_PASSWORD);
                                        }
                                        final var tokenInfo = UserJWTTokenInfo.builder()
                                                .id(user.getId())
                                                .username(user.getUsername())
                                                .fullName(user.getFullName())
                                                .build();
                                        String token = jwtAuth.generateToken(
                                                new JsonObject(this.gson.toJson(tokenInfo)),
                                                new JWTOptions().setAlgorithm("RS256").setExpiresInMinutes(14400)
                                        );
                                        return new UserLoginResponse(token, usersMapper.map(user));
                                    }
                            );
                    return future.toCompletionStage();
                }
        );
        return Future.fromCompletionStage(completionStage);
    }

    public Future<UsersEntity> validateUser(final RoutingContext context, final String username) {
        final var completionStage = sessionFactory.withSession(session ->
                {
                    CriteriaHandler<UsersEntity> usersEntityCriteriaHandler = (cb, cq, root) -> {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(
                                cb.equal(root.get("username"), username)
                        );
                        return cb.and(predicates.toArray(Predicate[]::new));
                    };
                    final var future = usersRepository.findOne(session, usersEntityCriteriaHandler)
                            .map(opt ->
                                    {
                                        if (opt.isEmpty()) {
                                            throw new InternalServiceException(AppError.USER_NOT_EXIST);
                                        }
                                        final var user = opt.get();
                                        if (user.getDeletedAt() != null) {
                                            throw new InternalServiceException(AppError.INACTIVE_USER);
                                        }
                                        return opt.get();
                                    }
                            );
                    return future.toCompletionStage();
                }
        );
        return Future.fromCompletionStage(completionStage);
    }

}
