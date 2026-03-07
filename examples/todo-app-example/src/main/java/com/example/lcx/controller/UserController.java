package com.example.lcx.controller;

import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.example.lcx.object.response.UserLoginResponse;
import com.example.lcx.service.UserService;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.io.lcx.common.annotation.Component;
import vn.io.lcx.vertx.base.annotation.process.Controller;
import vn.io.lcx.vertx.base.annotation.process.Post;
import vn.io.lcx.vertx.base.annotation.process.RequestBody;
import vn.io.lcx.vertx.base.annotation.process.RestController;
import vn.io.lcx.vertx.base.http.response.CommonResponse;

@Component
@RequiredArgsConstructor
@RestController(path = "/api/v2/user")
public class UserController {

    private final UserService userService;

    @Post(path = "/create_new")
    public Future<CommonResponse> createNew(RoutingContext ctx, @RequestBody CreateNewUserRequest req) {
        return userService.createNew(ctx, req).map(new CommonResponse());
    }

    @Post(path = "/login")
    public Future<UserLoginResponse> login(RoutingContext ctx, @RequestBody UserLoginRequest req) {
        return userService.login(ctx, req);
    }
}
