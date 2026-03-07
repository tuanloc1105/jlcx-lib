package com.example.controller;

import com.example.model.http.request.CreateNewUserRequest;
import com.example.model.http.request.UserLoginRequest;
import com.example.model.http.response.AppResponse;
import com.example.model.http.response.UserLoginResponse;
import com.example.service.UsersService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import vn.io.lcx.common.annotation.Component;
import vn.io.lcx.vertx.base.annotation.process.Post;
import vn.io.lcx.vertx.base.annotation.process.RequestBody;
import vn.io.lcx.vertx.base.annotation.process.RestController;

@Component
@RestController(path = "/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @Post(path = "/create_new")
    public Future<Void> createNew(@RequestBody final CreateNewUserRequest request) {
        return usersService.createNew(request);
    }

    @Post(path = "/login")
    public Future<AppResponse<UserLoginResponse>> login(@RequestBody final UserLoginRequest request) {
        return usersService.login(request).map(AppResponse::new);
    }

}
