package com.example.lcx.controller;

import com.example.lcx.object.request.CreateNewUserRequest;
import com.example.lcx.service.reactive.UserService;
import com.google.gson.Gson;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Post;

@Component
@RequiredArgsConstructor
@Controller(path = "/reactive-api/v1/user")
public class ReactiveUserController {

    private final UserService userService;
    private final Gson gson;

    @Post(path = "/create_new")
    public void createNew(RoutingContext ctx) {
        userService.createNew(
                ctx,
                gson.fromJson(
                        ctx.body()
                                .asString(CommonConstant.UTF_8_STANDARD_CHARSET),
                        CreateNewUserRequest.class
                )
        );
    }

}
