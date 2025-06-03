package com.example.lcx.service;

import com.example.lcx.object.dto.TaskDTO;
import com.example.lcx.object.request.CreateTaskRequest;
import com.example.lcx.object.request.DeleteTaskRequest;
import com.example.lcx.object.request.GetAllTaskRequest;
import com.example.lcx.object.request.GetTaskDetailRequest;
import com.example.lcx.object.request.MarkTaskAsFinishedRequest;
import com.example.lcx.object.request.SearchTasksByNameRequest;
import com.example.lcx.object.request.UpdateTaskRequest;
import vn.com.lcx.common.database.pageable.Page;

public interface TaskService {
    void createTask(final CreateTaskRequest request);

    TaskDTO getTaskDetail(final GetTaskDetailRequest request);

    Page<TaskDTO> searchTasksByName(final SearchTasksByNameRequest request);

    Page<TaskDTO> getAllTask(final GetAllTaskRequest request);

    void updateTask(final UpdateTaskRequest request);

    void deleteTask(final DeleteTaskRequest request);

    void markTaskAsFinished(final MarkTaskAsFinishedRequest request);
}
