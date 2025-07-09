package com.example.lcx.controller;

import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.example.lcx.service.UserService;
import com.google.gson.reflect.TypeToken;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Auth;
import vn.com.lcx.vertx.base.annotation.process.Block;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Get;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.controller.BaseController;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@RequiredArgsConstructor
@Component
@Controller(path = "/api/v1/user")
@Block
public class UserController extends BaseController {

    private final UserService userService;

    @Post(path = "/create_new")
    public void createNew(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    userService.createNew(o);
                    return new CommonResponse();
                },
                new TypeToken<CreateNewUserRequest>() {
                }
        );
    }

    @Post(path = "/login")
    public void login(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> userService.login(o),
                new TypeToken<UserLoginRequest>() {
                }
        );
    }

    @Get(path = "/test")
    @Auth
    public void test(RoutingContext ctx) {
        ctx.end("hello");
    }

}
