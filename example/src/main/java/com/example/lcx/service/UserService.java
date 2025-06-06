package com.example.lcx.service;

import com.example.lcx.entity.UserEntity;
import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.example.lcx.object.response.UserLoginResponse;

import java.util.Optional;

public interface UserService {

    void createNew(final CreateNewUserRequest request);

    UserLoginResponse login(final UserLoginRequest request);

    Optional<UserEntity> getUserByUsername(final String username);

}
