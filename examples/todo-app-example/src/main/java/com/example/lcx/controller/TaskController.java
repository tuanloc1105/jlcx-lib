package com.example.lcx.controller;

import com.example.lcx.object.request.CreateTaskRequest;
import com.example.lcx.object.request.DeleteTaskRequest;
import com.example.lcx.object.request.DeleteTasksRequest;
import com.example.lcx.object.request.GetAllTaskRequest;
import com.example.lcx.object.request.GetTaskDetailRequest;
import com.example.lcx.object.request.MarkTaskAsFinishedRequest;
import com.example.lcx.object.request.SearchTasksByNameRequest;
import com.example.lcx.object.request.UpdateTaskRequest;
import com.example.lcx.object.response.GetAllTaskResponse2;
import com.example.lcx.object.response.GetTaskDetailResponse2;
import com.example.lcx.object.response.SearchTasksByNameResponse2;
import com.example.lcx.service.TaskService;
import com.google.gson.Gson;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Auth;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.annotation.process.RequestBody;
import vn.com.lcx.vertx.base.annotation.process.RestController;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@RequiredArgsConstructor
@Component
@RestController(path = "/api/v2/task")
public class TaskController {

    private final TaskService taskService;

    @Post(path = "/create_task")
    @Auth
    public Future<CommonResponse> createTask(RoutingContext ctx, @RequestBody CreateTaskRequest req) {
        return taskService.createTask(ctx, req).map(new CommonResponse());
    }

    @Post(path = "/get_task_detail")
    @Auth
    public Future<GetTaskDetailResponse2> getTaskDetail(RoutingContext ctx, @RequestBody GetTaskDetailRequest req) {
        return taskService.getTaskDetail(ctx, req).map(GetTaskDetailResponse2::new);
    }

    @Post(path = "/search_tasks_by_name")
    @Auth
    public Future<SearchTasksByNameResponse2> searchTasksByName(RoutingContext ctx, @RequestBody SearchTasksByNameRequest req) {
        return taskService.searchTasksByName(ctx, req).map(SearchTasksByNameResponse2::new);
    }

    @Post(path = "/get_all_task")
    @Auth
    public Future<GetAllTaskResponse2> getAllTask(RoutingContext ctx, @RequestBody GetAllTaskRequest req) {
        return taskService.getAllTask(ctx, req).map(GetAllTaskResponse2::new);
    }

    @Post(path = "/update_task")
    @Auth
    public Future<CommonResponse> updateTask(RoutingContext ctx, @RequestBody UpdateTaskRequest req) {
        return taskService.updateTask(ctx, req).map(new CommonResponse());
    }

    @Post(path = "/delete_task")
    @Auth
    public Future<CommonResponse> deleteTask(RoutingContext ctx, @RequestBody DeleteTaskRequest req) {
        return taskService.deleteTask(ctx, req).map(new CommonResponse());
    }

    @Post(path = "/delete_tasks")
    @Auth
    public Future<CommonResponse> deleteTasks(RoutingContext ctx, @RequestBody DeleteTasksRequest req) {
        return taskService.deleteTasks(ctx, req).map(new CommonResponse());
    }

    @Post(path = "/mark_task_as_finished")
    @Auth
    public Future<CommonResponse> markTaskAsFinished(RoutingContext ctx, @RequestBody MarkTaskAsFinishedRequest req) {
        return taskService.markTaskAsFinished(ctx, req).map(new CommonResponse());
    }

}
