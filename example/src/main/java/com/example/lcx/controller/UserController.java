package com.example.lcx.controller;

import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.object.request.UserLoginRequest;
import com.google.gson.reflect.TypeToken;
import io.vertx.ext.web.RoutingContext;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Block;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.controller.BaseController;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

@Component
@Controller(path = "/api/v1/user")
@Block
public class UserController extends BaseController {

    @Post(path = "/create_new")
    public void createNew(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    throw new InternalServiceException(410, 410, "API is deprecated, please use v2 instead");
                },
                new TypeToken<CreateNewUserRequest>() {
                }
        );
    }

    @Post(path = "/login")
    public void login(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    throw new InternalServiceException(410, 410, "API is deprecated, please use v2 instead");
                },
                new TypeToken<UserLoginRequest>() {
                }
        );
    }

}
