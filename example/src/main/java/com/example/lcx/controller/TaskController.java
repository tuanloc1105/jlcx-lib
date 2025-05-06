package com.example.lcx.controller;

import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateTaskRequest;
import com.example.lcx.object.request.DeleteTaskRequest;
import com.example.lcx.object.request.GetAllTaskRequest;
import com.example.lcx.object.request.GetTaskDetailRequest;
import com.example.lcx.object.request.MarkTaskAsFinishedRequest;
import com.example.lcx.object.request.SearchTasksByNameRequest;
import com.example.lcx.object.request.UpdateTaskRequest;
import com.example.lcx.object.response.GetAllTaskResponse;
import com.example.lcx.object.response.GetTaskDetailResponse;
import com.example.lcx.object.response.SearchTasksByNameResponse;
import com.example.lcx.service.TaskService;
import com.google.gson.reflect.TypeToken;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Auth;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.context.AuthContext;
import vn.com.lcx.vertx.base.controller.BaseController;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@Component
@Controller(path = "/api/v1/task")
public class TaskController extends BaseController {

    private final TaskService taskService;

    public TaskController(Vertx vertx, TaskService taskService) {
        super(vertx);
        this.taskService = taskService;
    }

    @Post(path = "/create_task")
    @Auth
    public void createTask(RoutingContext ctx) {
        this.executeThreadBlock(
                ctx,
                (routingContext, o) -> {
                    taskService.createTask(o);
                    return new CommonResponse();
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
                    final var data = taskService.getTaskDetail(o);
                    return new GetTaskDetailResponse(data);
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
                    final var data = taskService.searchTasksByName(o);
                    return new SearchTasksByNameResponse(data);
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
                    final var data = taskService.getAllTask(o);
                    return new GetAllTaskResponse(data);
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
                    taskService.updateTask(o);
                    return new CommonResponse();
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
                    taskService.deleteTask(o);
                    return new CommonResponse();
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
                    taskService.markTaskAsFinished(o);
                    return new CommonResponse();
                },
                new TypeToken<MarkTaskAsFinishedRequest>() {
                }
        );
    }

}
