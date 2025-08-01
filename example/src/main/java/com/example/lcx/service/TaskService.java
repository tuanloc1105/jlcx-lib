package com.example.lcx.service;

import com.example.lcx.entity.TaskEntity;
import com.example.lcx.entity.UserEntity;
import com.example.lcx.enums.AppError;
import com.example.lcx.mapper.TaskMapper;
import com.example.lcx.object.dto.ReactiveTaskDTO;
import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateTaskRequest;
import com.example.lcx.object.request.DeleteTaskRequest;
import com.example.lcx.object.request.DeleteTasksRequest;
import com.example.lcx.object.request.GetAllTaskRequest;
import com.example.lcx.object.request.GetTaskDetailRequest;
import com.example.lcx.object.request.MarkTaskAsFinishedRequest;
import com.example.lcx.object.request.SearchTasksByNameRequest;
import com.example.lcx.object.request.UpdateTaskRequest;
import com.example.lcx.respository.TaskRepository;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.pageable.Direction;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.PostgreSQLPageable;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.reactive.utils.TransactionUtils;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskService {
    private final Pool pool;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public Future<Void> createTask(final RoutingContext context, final CreateTaskRequest request) {
        return getUserFromContext(context)
                .compose(userJWTTokenInfo -> validateUserAndGetConnection(context, userJWTTokenInfo.getUsername()))
                .compose(userEntity -> {
                    final var currentDateTime = DateTimeUtils.generateCurrentTimeDefault();
                    final TaskEntity newTask = taskMapper.mapToReactiveEntity(request);
                    newTask.setUserId(userEntity.getId());
                    newTask.setCreatedBy(userEntity.getUsername());
                    newTask.setUpdatedBy(userEntity.getUsername());
                    newTask.setUpdatedAt(currentDateTime);
                    newTask.setCreatedAt(currentDateTime);

                    return TransactionUtils.executeInTransaction(pool, connection ->
                            taskRepository.save(context, connection, newTask)
                    ).map(it -> CommonConstant.VOID);
                });
    }

    public Future<ReactiveTaskDTO> getTaskDetail(final RoutingContext context, final GetTaskDetailRequest request) {
        return getUserFromContext(context)
                .compose(userJWTTokenInfo -> validateUserAndGetConnection(context, userJWTTokenInfo.getUsername()))
                .compose(userEntity ->
                        pool.getConnection()
                                .compose(connection ->
                                        findAndValidateTask(context, connection, BigInteger.valueOf(request.getId()), userEntity.getId())
                                                .map(taskMapper::mapToReactiveDTO)
                                                .eventually(connection::close)
                                )
                );
    }

    public Future<Page<ReactiveTaskDTO>> searchTasksByName(final RoutingContext context, final SearchTasksByNameRequest request) {
        return getUserFromContext(context)
                .compose(userJWTTokenInfo -> validateUserAndGetConnection(context, userJWTTokenInfo.getUsername()))
                .compose(userEntity ->
                        pool.getConnection()
                                .compose(connection ->
                                        taskRepository.searchTaskOfUser(
                                                        context,
                                                        connection,
                                                        request.getSearchContent(),
                                                        userEntity.getId(),
                                                        PostgreSQLPageable.builder()
                                                                .pageSize(10)
                                                                .pageNumber(request.getPageNumber())
                                                                .entityClass(TaskEntity.class)
                                                                .build().add("id", Direction.DESC)
                                                ).compose(taskEntity -> {
                                                            final var dtos = taskEntity.stream()
                                                                    .map(taskMapper::mapToReactiveDTO)
                                                                    .collect(Collectors.toCollection(ArrayList::new));
                                                            return taskRepository.countSearchTaskOfUser(
                                                                            context,
                                                                            connection,
                                                                            request.getSearchContent(),
                                                                            userEntity.getId()
                                                                    )
                                                                    .compose(
                                                                            totalElement ->
                                                                                    Future.succeededFuture(
                                                                                            Page.create(dtos, totalElement, request.getPageNumber(), 10)
                                                                                    )
                                                                    );
                                                        }
                                                )
                                                .eventually(connection::close)
                                )
                );
    }

    public Future<Page<ReactiveTaskDTO>> getAllTask(final RoutingContext context, final GetAllTaskRequest request) {
        return getUserFromContext(context)
                .compose(userJWTTokenInfo -> validateUserAndGetConnection(context, userJWTTokenInfo.getUsername()))
                .compose(userEntity ->
                        pool.getConnection()
                                .compose(connection ->
                                        taskRepository.getTasksOfUser(
                                                        context,
                                                        connection,
                                                        userEntity.getId(),
                                                        PostgreSQLPageable.builder()
                                                                .pageSize(10)
                                                                .pageNumber(request.getPageNumber())
                                                                .entityClass(TaskEntity.class)
                                                                .build().add("id", Direction.DESC)
                                                ).compose(taskEntity -> {
                                                            final var dtos = taskEntity.stream()
                                                                    .map(taskMapper::mapToReactiveDTO)
                                                                    .collect(Collectors.toCollection(ArrayList::new));
                                                            return taskRepository.countTasksOfUser(
                                                                            context,
                                                                            connection,
                                                                            userEntity.getId()
                                                                    )
                                                                    .compose(
                                                                            totalElement ->
                                                                                    Future.succeededFuture(
                                                                                            Page.create(dtos, totalElement, request.getPageNumber(), 10)
                                                                                    )
                                                                    );
                                                        }
                                                )
                                                .eventually(connection::close)
                                )
                );
    }

    public Future<Void> updateTask(final RoutingContext context, final UpdateTaskRequest request) {
        return getUserFromContext(context)
                .compose(userJWTTokenInfo -> validateUserAndGetConnection(context, userJWTTokenInfo.getUsername()))
                .compose(userEntity ->
                        TransactionUtils.executeInTransaction(pool, connection ->
                                findAndValidateTask(context, connection, BigInteger.valueOf(request.getId()), userEntity.getId())
                                        .compose(task -> {
                                            final var currentDateTime = DateTimeUtils.generateCurrentTimeDefault();
                                            task.setTaskName(request.getTaskName());
                                            task.setTaskDetail(request.getTaskDetail());
                                            task.setRemindAt(request.getRemindAt());
                                            task.setUpdatedBy(userEntity.getUsername());
                                            task.setUpdatedAt(currentDateTime);
                                            return taskRepository.update(context, connection, task).map(it -> CommonConstant.VOID);
                                        })
                        )
                );
    }

    public Future<Void> deleteTask(final RoutingContext context, final DeleteTaskRequest request) {
        return getUserFromContext(context)
                .compose(userJWTTokenInfo -> validateUserAndGetConnection(context, userJWTTokenInfo.getUsername()))
                .compose(userEntity ->
                        TransactionUtils.executeInTransaction(pool, connection ->
                                findAndValidateTask(context, connection, BigInteger.valueOf(request.getId()), userEntity.getId())
                                        .compose(task -> {
                                            task.setUpdatedBy(userEntity.getUsername());
                                            task.setDeletedAt(DateTimeUtils.generateCurrentTimeDefault());
                                            return taskRepository.update(context, connection, task).map(it -> CommonConstant.VOID);
                                        })
                        )
                );
    }

    public Future<Void> deleteTasks(final RoutingContext context, final DeleteTasksRequest request) {
        return getUserFromContext(context)
                .compose(userJWTTokenInfo -> validateUserAndGetConnection(context, userJWTTokenInfo.getUsername()))
                .compose(userEntity ->
                        TransactionUtils.executeInTransaction(pool, connection -> {
                                    Future<Void> chain = Future.succeededFuture();
                                    for (Long id : request.getId()) {
                                        chain = chain.compose(v ->
                                                findAndValidateTask(context, connection, BigInteger.valueOf(id), userEntity.getId())
                                                        .compose(task -> {
                                                            task.setUpdatedBy(userEntity.getUsername());
                                                            task.setDeletedAt(DateTimeUtils.generateCurrentTimeDefault());
                                                            return taskRepository.update(context, connection, task).map(it -> CommonConstant.VOID);
                                                        })
                                        );
                                    }
                                    return chain;
                                }
                        )
                );
    }

    public Future<Void> markTaskAsFinished(final RoutingContext context, final MarkTaskAsFinishedRequest request) {
        return getUserFromContext(context)
                .compose(userJWTTokenInfo -> validateUserAndGetConnection(context, userJWTTokenInfo.getUsername()))
                .compose(userEntity ->
                        TransactionUtils.executeInTransaction(pool, connection ->
                                findAndValidateTask(context, connection, BigInteger.valueOf(request.getId()), userEntity.getId())
                                        .compose(task -> {
                                            if (task.getFinished()) {
                                                return Future.failedFuture(new InternalServiceException(AppError.TASK_ALREADY_FINISHED));
                                            }
                                            task.setUpdatedBy(userEntity.getUsername());
                                            task.setUpdatedAt(DateTimeUtils.generateCurrentTimeDefault());
                                            task.setFinished(true);
                                            return taskRepository.update(context, connection, task).map(it -> CommonConstant.VOID);
                                        })
                        )
                );
    }

    // Helper methods
    private Future<UserJWTTokenInfo> getUserFromContext(final RoutingContext context) {
        // A note for how to use Promise
        // final var promise = Promise.<UserJWTTokenInfo>promise();
        // final var userInfo = context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER);
        // if (userInfo == null) {
        //     promise.fail(new InternalServiceException(AppError.UNKNOWN_USER));
        // }
        // promise.succeed(userInfo);
        // return promise.future();

        return Future.succeededFuture(context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER))
                .compose(it -> {
                    if (it == null) {
                        return Future.failedFuture(new InternalServiceException(AppError.UNKNOWN_USER));
                    }
                    return Future.succeededFuture(it);
                });
    }

    private Future<UserEntity> validateUserAndGetConnection(final RoutingContext context, final String username) {
        return pool.getConnection()
                .compose(connection ->
                        userService.validateUser(context, connection, username)
                                .eventually(connection::close)
                );
    }

    private Future<TaskEntity> findAndValidateTask(final RoutingContext context,
                                                   final SqlConnection connection, final BigInteger taskId, final BigInteger userId) {
        return taskRepository.findTaskDetailOfUser(context, connection, taskId, userId)
                .compose(taskEntity -> {
                    if (taskEntity.isEmpty()) {
                        return Future.failedFuture(new InternalServiceException(AppError.TASK_NOT_FOUND));
                    }
                    final var task = taskEntity.get();
                    if (task.getDeletedAt() != null) {
                        return Future.failedFuture(new InternalServiceException(AppError.TASK_ALREADY_DELETED));
                    }
                    return Future.succeededFuture(task);
                });
    }
}
