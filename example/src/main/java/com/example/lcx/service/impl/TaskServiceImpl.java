package com.example.lcx.service.impl;

import com.example.lcx.entity.Task;
import com.example.lcx.enums.AppError;
import com.example.lcx.mapper.TaskMapper;
import com.example.lcx.object.dto.TaskDTO;
import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateTaskRequest;
import com.example.lcx.object.request.DeleteTaskRequest;
import com.example.lcx.object.request.GetAllTaskRequest;
import com.example.lcx.object.request.GetTaskDetailRequest;
import com.example.lcx.object.request.MarkTaskAsFinishedRequest;
import com.example.lcx.object.request.SearchTasksByNameRequest;
import com.example.lcx.object.request.UpdateTaskRequest;
import com.example.lcx.respository.TaskRepository;
import com.example.lcx.service.TaskService;
import com.example.lcx.service.UserService;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Service;
import vn.com.lcx.common.annotation.Transaction;
import vn.com.lcx.common.database.pageable.Direction;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.PostgreSQLPageable;
import vn.com.lcx.common.database.specification.Specification;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.vertx.base.context.AuthContext;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;

    @Transaction
    public void createTask(final CreateTaskRequest request) {
        final var currentDateTime = DateTimeUtils.generateCurrentTimeDefault();
        if (request.getRemindAt().isBefore(currentDateTime)) {
            throw new InternalServiceException(AppError.INVALID_REMIND_TIME);
        }
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var user = userService.getUserByUsername(currentUser.getUsername());
        final var newTask = taskMapper.map(request);
        newTask.setUserId(user.getId());
        newTask.setCreatedBy(currentUser.getUsername());
        newTask.setUpdatedBy(currentUser.getUsername());
        taskRepository.save2(newTask);
    }

    public TaskDTO getTaskDetail(final GetTaskDetailRequest request) {
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        return taskMapper.map(
                taskRepository.findByIdAndFinishedAndCreatedBy(
                        request.getId(),
                        false,
                        currentUser.getUsername()
                )
        );
    }

    public Page<TaskDTO> searchTasksByName(final SearchTasksByNameRequest request) {
        final var page = PostgreSQLPageable.builder()
                .entityClass(Task.class)
                .pageNumber(request.getPageNumber())
                .pageSize(10)
                .build();
        page.add("id", Direction.DESC);
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var result = taskRepository.find(
                Specification.create(Task.class)
                        .like("taskName", request.getSearchContent())
                        .and(
                                Specification.create(Task.class)
                                        .equal("createdBy", currentUser.getUsername())
                        ),
                page
        );
        return Page.<Task, TaskDTO>create(result, taskMapper::map);
    }

    public Page<TaskDTO> getAllTask(final GetAllTaskRequest request) {
        final var page = PostgreSQLPageable.builder()
                .entityClass(Task.class)
                .pageNumber(request.getPageNumber())
                .pageSize(10)
                .build();
        page.add("id", Direction.DESC);
        page.fieldToColumn();
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var result = taskRepository.find(
                Specification.create(Task.class).equal("createdBy", currentUser.getUsername()),
                page
        );
        //noinspection RedundantTypeArguments
        return Page.<Task, TaskDTO>create(result, taskMapper::map);
    }

    @Transaction
    public void updateTask(final UpdateTaskRequest request) {
        final var currentDateTime = DateTimeUtils.generateCurrentTimeDefault();
        if (request.getRemindAt().isBefore(currentDateTime)) {
            throw new InternalServiceException(AppError.INVALID_REMIND_TIME);
        }
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var findTaskResult = taskRepository.find(
                Specification.create(Task.class)
                        .equal("createdBy", currentUser.getUsername())
                        .and(
                                Specification.create(Task.class)
                                        .equal("id", request.getId())
                        )
                        .and(
                                Specification.create(Task.class)
                                        .equal("finished", false)
                        )
        );
        if (findTaskResult.isEmpty()) {
            throw new InternalServiceException(AppError.TASK_NOT_FOUND);
        }
        final var task = findTaskResult.get(0);
        task.setTaskName(request.getTaskName());
        task.setTaskDetail(request.getTaskDetail());
        task.setRemindAt(request.getRemindAt());
        task.setUpdatedBy(currentUser.getUsername());
        task.setUpdatedTime(currentDateTime);
        taskRepository.update(task);
    }

    @Transaction
    public void deleteTask(final DeleteTaskRequest request) {
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var task = taskRepository.findByIdAndCreatedBy(
                request.getId(),
                currentUser.getUsername()
        );
        if (task.getId() == null || task.getId() == 0L) {
            throw new InternalServiceException(AppError.TASK_NOT_FOUND);
        }
        taskRepository.delete(task);
    }

    @Transaction
    public void markTaskAsFinished(final MarkTaskAsFinishedRequest request) {
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var task = taskRepository.findByIdAndCreatedBy(
                request.getId(),
                currentUser.getUsername()
        );
        if (task.getId() == null || task.getId() == 0L) {
            throw new InternalServiceException(AppError.TASK_NOT_FOUND);
        }
        if (task.getFinished()) {
            throw new InternalServiceException(AppError.TASK_ALREADY_FINISHED);
        }
        task.setFinished(true);
        taskRepository.update(task);
    }

}
