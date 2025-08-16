package com.example.lcx.controller;

import com.example.lcx.object.request.CreateTaskRequest;
import com.example.lcx.object.request.DeleteTaskRequest;
import com.example.lcx.object.request.GetAllTaskRequest;
import com.example.lcx.object.request.GetTaskDetailRequest;
import com.example.lcx.object.request.MarkTaskAsFinishedRequest;
import com.example.lcx.object.request.SearchTasksByNameRequest;
import com.example.lcx.object.request.UpdateTaskRequest;
import com.google.gson.reflect.TypeToken;
import io.vertx.ext.web.RoutingContext;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Auth;
import vn.com.lcx.vertx.base.annotation.process.Block;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.controller.BaseController;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

@Component
@Controller(path = "/api/v1/task")
@Block
public class TaskController extends BaseController {

    @Post(path = "/create_task")
    @Auth
    public void createTask(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    throw new InternalServiceException(410, 410, "API is deprecated, please use v2 instead");
                },
                new TypeToken<CreateTaskRequest>() {
                }
        );
    }

    @Post(path = "/get_task_detail")
    @Auth
    public void getTaskDetail(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    throw new InternalServiceException(410, 410, "API is deprecated, please use v2 instead");
                },
                new TypeToken<GetTaskDetailRequest>() {
                }
        );
    }

    @Post(path = "/search_tasks_by_name")
    @Auth
    public void searchTasksByName(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    throw new InternalServiceException(410, 410, "API is deprecated, please use v2 instead");
                },
                new TypeToken<SearchTasksByNameRequest>() {
                }
        );
    }

    @Post(path = "/get_all_task")
    @Auth
    public void getAllTask(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    throw new InternalServiceException(410, 410, "API is deprecated, please use v2 instead");
                },
                new TypeToken<GetAllTaskRequest>() {
                }
        );
    }

    @Post(path = "/update_task")
    @Auth
    public void updateTask(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    throw new InternalServiceException(410, 410, "API is deprecated, please use v2 instead");
                },
                new TypeToken<UpdateTaskRequest>() {
                }
        );
    }

    @Post(path = "/delete_task")
    @Auth
    public void deleteTask(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    throw new InternalServiceException(410, 410, "API is deprecated, please use v2 instead");
                },
                new TypeToken<DeleteTaskRequest>() {
                }
        );
    }

    @Post(path = "/mark_task_as_finished")
    @Auth
    public void markTaskAsFinished(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    throw new InternalServiceException(410, 410, "API is deprecated, please use v2 instead");
                },
                new TypeToken<MarkTaskAsFinishedRequest>() {
                }
        );
    }

}
