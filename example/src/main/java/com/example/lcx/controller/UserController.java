package com.example.lcx.controller;

import com.example.lcx.http.request.CreateUserRequest;
import com.example.lcx.http.request.FindUserRequest;
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

    /**
     * Example:
     * <pre>
     *     curl --location --request POST 'http://localhost:8123/user/save' \
     *     --header 'Content-Type: application/json' \
     *     --data '{
     *         "firstName": "1744969003302",
     *         "lastName": "1f287066-893d-48ed-8ecb-6a22dc5ccd2e",
     *         "age": 6205
     *     }'
     * </pre>
     * @param routingContext
     */
    @Post(path = "/save")
    public void save(RoutingContext routingContext) {
        this.executeThreadBlock(
                routingContext,
                (routingContext1, o) -> new UserResponse(this.userService.save(o)),
                new TypeToken<CreateUserRequest>() {
                }
        );
    }

    /**
     * Example:
     * <pre>
     *     curl --location --request GET 'http://localhost:8123/user/find_by_id?id=22'
     * </pre>
     * @param routingContext
     */
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

    /**
     * Example:
     * <pre>
     *     curl --location --request GET 'http://localhost:8123/user/find_all?pageNumber=1&pageSize=10'
     * </pre>
     * @param routingContext
     */
    @Get(path = "/find_all")
    public void findAll(RoutingContext routingContext) {
        this.executeThreadBlock(
                routingContext,
                (routingContext1, o) -> {
                    final int pageNumber = this.getRequestQueryParam(routingContext, "pageNumber", i -> {
                        try {
                            return Integer.parseInt(i);
                        } catch (Exception e) {
                            return 1;
                        }
                    });
                    final int pageSize = this.getRequestQueryParam(routingContext, "pageSize", i -> {
                        try {
                            return Integer.parseInt(i);
                        } catch (Exception e) {
                            return 10;
                        }
                    });
                    return new UserListResponse(this.userService.findAll(pageNumber, pageSize));
                },
                VOID
        );
    }

    /**
     * Example:
     * <pre>
     *     curl --location --request POST 'http://localhost:8123/user/find_user' \
     *     --header 'Content-Type: application/json' \
     *     --data '{
     *         "firstName": "1741844188294",
     *         "lastName": "ba89b2b5-8016-437c-a74b-36d251338605",
     *         "age": 38286
     *     }'
     * </pre>
     * @param routingContext
     */
    @Post(path = "/find_user")
    public void findUser(RoutingContext routingContext) {
        this.executeThreadBlock(
                routingContext,
                (routingContext1, o) -> userService.findUser(o),
                new TypeToken<FindUserRequest>() {
                }
        );
    }

    /**
     * Example:
     * <pre>
     *     curl --location --request PUT 'http://localhost:8123/user/update' \
     *     --header 'Content-Type: application/json' \
     *     --data '{
     *         "id": 22,
     *         "firstName": "1744969003302",
     *         "lastName": "1f287066-893d-48ed-8ecb-6a22dc5ccd2e",
     *         "age": 6205
     *     }'
     * </pre>
     * @param routingContext
     */
    @Put(path = "/update")
    public void update(RoutingContext routingContext) {
        this.executeThreadBlock(
                routingContext,
                (routingContext1, o) -> new UserResponse(this.userService.update(o)),
                new TypeToken<UpdateUserRequest>() {
                }
        );
    }

    /**
     * Example:
     * <pre>
     *     curl --location --request DELETE 'http://localhost:8123/user/delete?id=28'
     * </pre>
     * @param routingContext
     */
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
