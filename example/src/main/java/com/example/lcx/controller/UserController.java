package com.example.lcx.controller;

import com.example.lcx.http.request.CreateUserRequest;
import com.example.lcx.http.request.UpdateUserRequest;
import com.example.lcx.http.response.UserListResponse;
import com.example.lcx.http.response.UserResponse;
import com.example.lcx.service.UserService;
import com.google.gson.reflect.TypeToken;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import lombok.val;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Delete;
import vn.com.lcx.vertx.base.annotation.process.Get;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.annotation.process.Put;
import vn.com.lcx.vertx.base.controller.BaseController;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@Controller(path = "/user")
@Component
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(Vertx vertx, UserService userService) {
        super(vertx);
        this.userService = userService;
    }

    @Post(path = "/save")
    public void save(RoutingContext routingContext) {
        this.executeThreadBlock(
                routingContext,
                (routingContext1, o) -> new UserResponse(this.userService.save(o)),
                new TypeToken<CreateUserRequest>() {
                }
        );
    }

    @Get(path = "/find_by_id")
    public void findById(RoutingContext routingContext) {
        this.executeThreadBlock(
                routingContext,
                (routingContext1, o) -> {
                    val id = this.getRequestQueryParam(routingContext, "id", i -> {
                        try {
                            return Long.parseLong(i);
                        } catch (Exception e) {
                            return 0L;
                        }
                    });
                    return new UserResponse(this.userService.findById(id));
                },
                VOID
        );
    }

    @Get(path = "/find_all")
    public void findAll(RoutingContext routingContext) {
        this.executeThreadBlock(
                routingContext,
                (routingContext1, o) -> {
                    return new UserListResponse(this.userService.findAll());
                },
                VOID
        );
    }

    @Put(path = "/update")
    public void update(RoutingContext routingContext) {
        this.executeThreadBlock(
                routingContext,
                (routingContext1, o) -> new UserResponse(this.userService.update(o)),
                new TypeToken<UpdateUserRequest>() {
                }
        );
    }

    @Delete(path = "/delete")
    public void delete(RoutingContext routingContext) {
        this.executeThreadBlock(
                routingContext,
                (routingContext1, o) -> {
                    val id = this.getRequestQueryParam(routingContext, "id", i -> {
                        try {
                            return Long.parseLong(i);
                        } catch (Exception e) {
                            return 0L;
                        }
                    });
                    this.userService.deleteById(id);
                    return new CommonResponse();
                },
                VOID
        );
    }

}
