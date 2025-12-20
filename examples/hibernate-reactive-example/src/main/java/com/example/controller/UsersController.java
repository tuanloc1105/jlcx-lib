package com.example.controller;

import com.example.model.http.request.CreateNewUserRequest;
import com.example.model.http.request.UserLoginRequest;
import com.example.model.http.response.AppResponse;
import com.example.model.http.response.UserLoginResponse;
import com.example.service.UsersService;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.annotation.process.RequestBody;
import vn.com.lcx.vertx.base.annotation.process.RestController;

@Component
@RestController(path = "/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @Post(path = "/create_new")
    public Future<Void> createNew(final RoutingContext context, @RequestBody final CreateNewUserRequest request) {
        return usersService.createNew(context, request);
    }

    @Post(path = "/login")
    public Future<AppResponse<UserLoginResponse>> login(final RoutingContext context, @RequestBody final UserLoginRequest request) {
        return usersService.login(context, request).map(AppResponse::new);
    }

}
