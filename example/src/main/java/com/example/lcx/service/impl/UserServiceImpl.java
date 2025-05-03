package com.example.lcx.service.impl;

import com.example.lcx.entity.User;
import com.example.lcx.enums.AppError;
import com.example.lcx.mapper.UserMapper;
import com.example.lcx.object.dto.UserDTO;
import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.example.lcx.object.response.UserLoginResponse;
import com.example.lcx.respository.UserRepository;
import com.example.lcx.service.UserService;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Service;
import vn.com.lcx.common.annotation.Transaction;
import vn.com.lcx.common.database.specification.SimpleSpecificationImpl;
import vn.com.lcx.common.database.specification.Specification;
import vn.com.lcx.common.utils.BCryptUtils;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

@Service
@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JWTAuth jwtAuth;
    private final Gson gson;

    @Transaction
    public void createNew(final CreateNewUserRequest request) {
        Specification specification = SimpleSpecificationImpl.of(User.class).equal("username", request.getUsername());
        final var userExistedInDB = userRepository.find(specification);
        if (!userExistedInDB.isEmpty()) {
            throw new InternalServiceException(AppError.USER_EXISTED);
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(BCryptUtils.hashPassword(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setActive(true);
        userRepository.save2(user);
    }

    public UserLoginResponse login(final UserLoginRequest request) {
        Specification specification = SimpleSpecificationImpl.of(User.class)
                .equal("username", request.getUsername());
        final var userExistedInDB = userRepository.find(specification);
        if (userExistedInDB.isEmpty()) {
            throw new InternalServiceException(AppError.USER_NOT_EXIST);
        }
        if (userExistedInDB.size() > 1) {
            throw new InternalServiceException(AppError.UNKNOWN_USER);
        }
        final var user = userExistedInDB.get(0);
        if (!user.getActive()) {
            throw new InternalServiceException(AppError.USER_IS_NOT_ACTIVE);
        }
        try {
            BCryptUtils.comparePassword(request.getPassword(), user.getPassword());
            final var tokenInfo = UserJWTTokenInfo.builder()
                    .id(user.getId())
                    .active(user.getActive())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .build();
            String token = this.jwtAuth.generateToken(
                    new JsonObject(this.gson.toJson(tokenInfo)),
                    new JWTOptions().setAlgorithm("RS256").setExpiresInMinutes(14400)
            );
            return new UserLoginResponse(token, user.getFullName());
        } catch (IllegalArgumentException e) {
            throw new InternalServiceException(AppError.INCORRECT_PASSWORD);
        }
    }

    @Transaction
    public UserDTO getUserByUsername(final String username) {
        return userMapper.map(userRepository.findByUsernameAndActive(username, true));
    }

}
