package com.example.lcx.controller;

import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.example.lcx.service.reactive.UserService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.controller.ReactiveController;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@Component
@RequiredArgsConstructor
@Controller(path = "/reactive-api/v1/user")
public class ReactiveUserController extends ReactiveController {
    private final UserService userService;
    private final Gson gson;

    @Post(path = "/create_new")
    public void createNew(RoutingContext ctx) {
        try {
            CreateNewUserRequest req = gson.fromJson(ctx.body().asString(), CreateNewUserRequest.class);
            userService.createNew(ctx, req).onSuccess(user -> {
                handleResponse(ctx, gson, new CommonResponse());
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }

    @Post(path = "/login")
    public void login(RoutingContext ctx) {
        try {
            UserLoginRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            userService.login(ctx, req).onSuccess(loginResp -> {
                handleResponse(ctx, gson, loginResp);
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }
}
