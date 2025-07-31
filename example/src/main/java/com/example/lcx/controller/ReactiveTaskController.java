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
import com.google.gson.reflect.TypeToken;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Auth;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.controller.ReactiveController;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@RequiredArgsConstructor
@Component
@Controller(path = "/api/v2/task")
public class ReactiveTaskController extends ReactiveController {

    private final TaskService taskService;
    private final Gson gson;

    @Post(path = "/create_task")
    @Auth
    public void createTask(RoutingContext ctx) {
        try {
            CreateTaskRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            taskService.createTask(ctx, req).onSuccess(v -> {
                handleResponse(ctx, gson, new CommonResponse());
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }

    }

    @Post(path = "/get_task_detail")
    @Auth
    public void getTaskDetail(RoutingContext ctx) {
        try {
            GetTaskDetailRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            taskService.getTaskDetail(ctx, req).onSuccess(taskDTO -> {
                final var resp = new GetTaskDetailResponse2(taskDTO);
                handleResponse(ctx, gson, resp);
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }

    @Post(path = "/search_tasks_by_name")
    @Auth
    public void searchTasksByName(RoutingContext ctx) {
        try {
            SearchTasksByNameRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            taskService.searchTasksByName(ctx, req).onSuccess(reactiveTaskDTOPage -> {
                final var response = new SearchTasksByNameResponse2(reactiveTaskDTOPage);
                handleResponse(ctx, gson, response);
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }

    @Post(path = "/get_all_task")
    @Auth
    public void getAllTask(RoutingContext ctx) {
        try {
            GetAllTaskRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            taskService.getAllTask(ctx, req).onSuccess(reactiveTaskDTOPage -> {
                final var resp = new GetAllTaskResponse2(reactiveTaskDTOPage);
                handleResponse(ctx, gson, resp);
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }

    @Post(path = "/update_task")
    @Auth
    public void updateTask(RoutingContext ctx) {
        try {
            UpdateTaskRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            taskService.updateTask(ctx, req).onSuccess(v -> {
                handleResponse(ctx, gson, new CommonResponse());
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }

    @Post(path = "/delete_task")
    @Auth
    public void deleteTask(RoutingContext ctx) {
        try {
            DeleteTaskRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            taskService.deleteTask(ctx, req).onSuccess(v -> {
                handleResponse(ctx, gson, new CommonResponse());
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }

    @Post(path = "/delete_tasks")
    @Auth
    public void deleteTasks(RoutingContext ctx) {
        try {
            DeleteTasksRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            taskService.deleteTasks(ctx, req).onSuccess(v -> {
                handleResponse(ctx, gson, new CommonResponse());
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }

    @Post(path = "/mark_task_as_finished")
    @Auth
    public void markTaskAsFinished(RoutingContext ctx) {
        try {
            MarkTaskAsFinishedRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            taskService.markTaskAsFinished(ctx, req).onSuccess(v -> {
                handleResponse(ctx, gson, new CommonResponse());
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }

}
