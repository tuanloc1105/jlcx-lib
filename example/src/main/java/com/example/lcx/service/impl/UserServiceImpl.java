package com.example.lcx.service.impl;

import com.example.lcx.entity.UserEntity;
import com.example.lcx.enums.AppError;
import com.example.lcx.mapper.UserMapper;
import com.example.lcx.object.dto.UserDTO;
import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.example.lcx.object.response.UserLoginResponse;
import com.example.lcx.respository.UserRepo;
import com.example.lcx.respository.UserRepository;
import com.example.lcx.service.UserService;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.utils.BCryptUtils;
import vn.com.lcx.jpa.annotation.Service;
import vn.com.lcx.jpa.annotation.Transactional;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.util.ArrayList;
import java.util.List;

@Service
@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final JWTAuth jwtAuth;
    private final Gson gson;

    @Transactional(onRollback = {Exception.class})
    public void createNew(final CreateNewUserRequest request) {
        var userOptional = userRepo.findOne(
                (cb, cq, root) ->
                        cb.equal(root.get("username"), request.getUsername())
        );
        if (userOptional.isPresent()) {
            throw new InternalServiceException(AppError.USER_EXISTED);
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(BCryptUtils.hashPassword(request.getPassword()));
        user.setFullName(request.getFullName());
        userRepo.save(user);
    }

    public UserLoginResponse login(final UserLoginRequest request) {
        var userOptional = userRepo.findOne(
                (cb, cq, root) ->
                        cb.equal(root.get("username"), request.getUsername())
        );
        var user = userOptional.orElseThrow(() -> new InternalServiceException(AppError.USER_NOT_EXIST));
        BCryptUtils.comparePassword(request.getPassword(), user.getPassword());
        final var tokenInfo = UserJWTTokenInfo.builder()
                .id(user.getId().longValue())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .build();
        String token = this.jwtAuth.generateToken(
                new JsonObject(this.gson.toJson(tokenInfo)),
                new JWTOptions().setAlgorithm("RS256").setExpiresInMinutes(14400)
        );
        return new UserLoginResponse(token, user.getFullName());
    }

    public UserDTO getUserByUsername(final String username) {
        return userMapper.map(
                userRepo.findOne(
                        (cb, cq, root) -> {
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(cb.equal(root.get("username"), username));
                            predicates.add(cb.isNull(root.get("deletedAt")));
                            return cb.and(predicates.toArray(Predicate[]::new));
                        }
                ).orElse(new UserEntity())
        );
    }

}
