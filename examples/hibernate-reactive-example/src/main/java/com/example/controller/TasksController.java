package com.example.controller;

import com.example.model.dto.TaskDTO;
import com.example.model.http.request.CreateTaskRequest;
import com.example.model.http.request.GetAllTaskRequest;
import com.example.model.http.request.GetTaskDetailRequest;
import com.example.model.http.request.SearchTasksByNameRequest;
import com.example.model.http.request.UpdateTaskRequest;
import com.example.model.http.response.AppResponse;
import com.example.service.TasksService;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.annotation.process.RequestBody;
import vn.com.lcx.vertx.base.annotation.process.RestController;

@RestController(path = "/api/tasks")
@Component
@RequiredArgsConstructor
public class TasksController {

    private final TasksService tasksService;

    @Post(path = "/create_task")
    public Future<Void> createTask(RoutingContext context, @RequestBody CreateTaskRequest request) {
        return tasksService.createTask(context, request);
    }

    @Post(path = "/get_task_detail")
    public Future<AppResponse<TaskDTO>> getTaskDetail(RoutingContext context, @RequestBody GetTaskDetailRequest request) {
        return tasksService.getTaskDetail(context, request).map(AppResponse::new);
    }

    @Post(path = "/search_task_by_name")
    public Future<AppResponse<Page<TaskDTO>>> searchTaskByName(RoutingContext context, @RequestBody SearchTasksByNameRequest request) {
        return tasksService.searchTaskByName(context, request).map(AppResponse::new);
    }

    @Post(path = "/get_all_task")
    public Future<AppResponse<Page<TaskDTO>>> getAllTask(RoutingContext context, @RequestBody GetAllTaskRequest request) {
        return tasksService.getAllTask(context, request).map(AppResponse::new);
    }

    @Post(path = "/update_task")
    public Future<Void> updateTask(RoutingContext context, @RequestBody UpdateTaskRequest request) {
        return tasksService.updateTask(context, request);
    }

    @Post(path = "/delete_task")
    public Future<Void> deleteTask(RoutingContext context, @RequestBody UpdateTaskRequest request) {
        return tasksService.deleteTask(context, request);
    }

}
