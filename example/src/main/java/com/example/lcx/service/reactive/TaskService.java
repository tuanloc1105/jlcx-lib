package com.example.lcx.service.reactive;

import com.example.lcx.entity.reactive.TaskEntity;
import com.example.lcx.enums.AppError;
import com.example.lcx.mapper.TaskMapper;
import com.example.lcx.object.dto.ReactiveTaskDTO;
import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateTaskRequest;
import com.example.lcx.object.request.DeleteTaskRequest;
import com.example.lcx.object.request.GetAllTaskRequest;
import com.example.lcx.object.request.GetTaskDetailRequest;
import com.example.lcx.object.request.MarkTaskAsFinishedRequest;
import com.example.lcx.object.request.SearchTasksByNameRequest;
import com.example.lcx.object.request.UpdateTaskRequest;
import com.example.lcx.respository.reactive.TaskRepository;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.pageable.Direction;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.PostgreSQLPageable;
import vn.com.lcx.common.utils.DateTimeUtils;
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
        // final var promise = Promise.<Void>promise();
        // return Future.<UserJWTTokenInfo>future(
        //                 promise -> promise.complete(context.get(CommonConstant.CURRENT_USER))
        //         )
        return Future.succeededFuture(context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER))
                .compose(it -> {
                    if (it == null) {
                        return Future.failedFuture(new InternalServiceException(AppError.UNKNOWN_USER));
                    } else {
                        return Future.succeededFuture(it);
                    }
                })
                .compose(userJWTTokenInfo ->
                        pool.getConnection().compose(sqlConnection ->
                                userService.validateUser(context, sqlConnection, userJWTTokenInfo.getUsername())
                                        .compose(userEntity ->
                                                sqlConnection.begin()
                                                        .compose(transaction -> {
                                                            final var currentDateTime = DateTimeUtils.generateCurrentTimeDefault();
                                                            final TaskEntity newTask = taskMapper.mapToReactiveEntity(request);
                                                            newTask.setUserId(userEntity.getId());
                                                            newTask.setCreatedBy(userEntity.getUsername());
                                                            newTask.setUpdatedBy(userEntity.getUsername());
                                                            newTask.setUpdatedAt(currentDateTime);
                                                            newTask.setCreatedAt(currentDateTime);
                                                            return taskRepository.save(context, sqlConnection, newTask)
                                                                    .compose(taskEntity -> transaction.commit().map(it -> taskEntity))
                                                                    .onFailure(
                                                                            err -> transaction.rollback()
                                                                                    .onComplete(rollbackResult -> {
                                                                                        if (rollbackResult.failed()) {
                                                                                            err.addSuppressed(rollbackResult.cause());
                                                                                        }
                                                                                    })
                                                                    );
                                                        })
                                        )
                                        .map(it -> (Void) null)
                                        .eventually(sqlConnection::close)
                        )
                );
    }

    public Future<ReactiveTaskDTO> getTaskDetail(final RoutingContext context, final GetTaskDetailRequest request) {
        return Future.succeededFuture(context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER))
                .compose(it -> {
                    if (it == null) {
                        return Future.failedFuture(new InternalServiceException(AppError.UNKNOWN_USER));
                    } else {
                        return Future.succeededFuture(it);
                    }
                })
                .compose(userJWTTokenInfo ->
                        pool.getConnection()
                                .compose(sqlConnection ->
                                        userService.validateUser(context, sqlConnection, userJWTTokenInfo.getUsername())
                                                .compose(userEntity ->
                                                        taskRepository.findTaskDetailOfUser(
                                                                context,
                                                                sqlConnection,
                                                                BigInteger.valueOf(request.getId()),
                                                                userEntity.getId()
                                                        ).compose(taskEntity ->
                                                                taskEntity
                                                                        .map(it -> Future.succeededFuture(taskMapper.mapToReactiveDTO(it)))
                                                                        .orElse(Future.succeededFuture((new ReactiveTaskDTO())))
                                                        )
                                                )
                                                .eventually(sqlConnection::close)
                                )
                );
    }

    public Future<Page<ReactiveTaskDTO>> searchTasksByName(final RoutingContext context, final SearchTasksByNameRequest request) {
        return Future.succeededFuture(context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER))
                .compose(it -> {
                    if (it == null) {
                        return Future.failedFuture(new InternalServiceException(AppError.UNKNOWN_USER));
                    } else {
                        return Future.succeededFuture(it);
                    }
                })
                .compose(userJWTTokenInfo ->
                        pool.getConnection()
                                .compose(sqlConnection ->
                                        userService.validateUser(context, sqlConnection, userJWTTokenInfo.getUsername())
                                                .compose(userEntity ->
                                                        taskRepository.searchTaskOfUser(
                                                                context,
                                                                sqlConnection,
                                                                request.getSearchContent(),
                                                                userEntity.getId(),
                                                                PostgreSQLPageable.builder()
                                                                        .pageSize(10)
                                                                        .pageNumber(request.getPageNumber())
                                                                        .build().add("id", Direction.DESC)
                                                        ).compose(taskEntity -> {
                                                                    final var dtos = taskEntity.stream()
                                                                            .map(taskMapper::mapToReactiveDTO)
                                                                            .collect(Collectors.toCollection(ArrayList::new));
                                                                    return taskRepository.countSearchTaskOfUser(
                                                                                    context,
                                                                                    sqlConnection,
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
                                                )
                                                .eventually(sqlConnection::close)
                                )
                );
    }

    public Future<Page<ReactiveTaskDTO>> getAllTask(final RoutingContext context, final GetAllTaskRequest request) {
        return Future.succeededFuture(context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER))
                .compose(it -> {
                    if (it == null) {
                        return Future.failedFuture(new InternalServiceException(AppError.UNKNOWN_USER));
                    } else {
                        return Future.succeededFuture(it);
                    }
                })
                .compose(userJWTTokenInfo ->
                        pool.getConnection()
                                .compose(sqlConnection ->
                                        userService.validateUser(context, sqlConnection, userJWTTokenInfo.getUsername())
                                                .compose(userEntity ->
                                                        taskRepository.getTasksOfUser(
                                                                context,
                                                                sqlConnection,
                                                                userEntity.getId(),
                                                                PostgreSQLPageable.builder()
                                                                        .pageSize(10)
                                                                        .pageNumber(request.getPageNumber())
                                                                        .build().add("id", Direction.DESC)
                                                        ).compose(taskEntity -> {
                                                                    final var dtos = taskEntity.stream()
                                                                            .map(taskMapper::mapToReactiveDTO)
                                                                            .collect(Collectors.toCollection(ArrayList::new));
                                                                    return taskRepository.countTasksOfUser(
                                                                                    context,
                                                                                    sqlConnection,
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
                                                )
                                                .eventually(sqlConnection::close)
                                )
                );
    }

    public Future<Void> updateTask(final RoutingContext context, final UpdateTaskRequest request) {
        return Future.succeededFuture(context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER))
                .compose(it -> {
                    if (it == null) {
                        return Future.failedFuture(new InternalServiceException(AppError.UNKNOWN_USER));
                    } else {
                        return Future.succeededFuture(it);
                    }
                })
                .compose(userJWTTokenInfo ->
                        pool.getConnection()
                                .compose(connection ->
                                        userService.validateUser(context, connection, userJWTTokenInfo.getUsername())
                                                .eventually(connection::close)
                                ).compose(userEntity ->
                                        pool.getConnection()
                                                .compose(connection ->
                                                        connection.begin().compose(transaction ->
                                                                        taskRepository.findTaskDetailOfUser(context, connection, BigInteger.valueOf(request.getId()), userEntity.getId())
                                                                                .compose(taskEntity -> {
                                                                                    if (taskEntity.isEmpty()) {
                                                                                        return Future.failedFuture(new InternalServiceException(AppError.TASK_NOT_FOUND));
                                                                                    }
                                                                                    final var task = taskEntity.get();
                                                                                    if (task.getDeletedAt() != null) {
                                                                                        return Future.failedFuture(new InternalServiceException(AppError.TASK_ALREADY_DELETED));
                                                                                    }
                                                                                    final var currentDateTime = DateTimeUtils.generateCurrentTimeDefault();
                                                                                    task.setTaskName(request.getTaskName());
                                                                                    task.setTaskDetail(request.getTaskDetail());
                                                                                    task.setRemindAt(request.getRemindAt());
                                                                                    task.setUpdatedBy(userEntity.getUsername());
                                                                                    task.setUpdatedAt(currentDateTime);
                                                                                    return taskRepository.update(context, connection, task);
                                                                                })
                                                                                .compose(it -> transaction.commit())
                                                                                .onFailure(
                                                                                        err -> transaction.rollback()
                                                                                                .onComplete(rollbackResult -> {
                                                                                                    if (rollbackResult.failed()) {
                                                                                                        err.addSuppressed(rollbackResult.cause());
                                                                                                    }
                                                                                                })
                                                                                )

                                                                )
                                                                .eventually(connection::close)
                                                )
                                )
                );
    }

    public Future<Void> deleteTask(final RoutingContext context, final DeleteTaskRequest request) {
        return Future.succeededFuture(context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER))
                .compose(it -> {
                    if (it == null) {
                        return Future.failedFuture(new InternalServiceException(AppError.UNKNOWN_USER));
                    } else {
                        return Future.succeededFuture(it);
                    }
                })
                .compose(userJWTTokenInfo ->
                        pool.getConnection()
                                .compose(connection ->
                                        userService.validateUser(context, connection, userJWTTokenInfo.getUsername())
                                                .eventually(connection::close)
                                ).compose(userEntity ->
                                        pool.getConnection()
                                                .compose(connection ->
                                                        connection.begin().compose(transaction ->
                                                                        taskRepository.findTaskDetailOfUser(context, connection, BigInteger.valueOf(request.getId()), userEntity.getId())
                                                                                .compose(taskEntity -> {
                                                                                    if (taskEntity.isEmpty()) {
                                                                                        return Future.failedFuture(new InternalServiceException(AppError.TASK_NOT_FOUND));
                                                                                    }
                                                                                    final var task = taskEntity.get();
                                                                                    if (task.getDeletedAt() != null) {
                                                                                        return Future.failedFuture(new InternalServiceException(AppError.TASK_ALREADY_DELETED));
                                                                                    }
                                                                                    task.setUpdatedBy(userEntity.getUsername());
                                                                                    task.setDeletedAt(DateTimeUtils.generateCurrentTimeDefault());
                                                                                    return taskRepository.update(context, connection, task);
                                                                                })
                                                                                .compose(it -> transaction.commit())
                                                                                .onFailure(
                                                                                        err -> transaction.rollback()
                                                                                                .onComplete(rollbackResult -> {
                                                                                                    if (rollbackResult.failed()) {
                                                                                                        err.addSuppressed(rollbackResult.cause());
                                                                                                    }
                                                                                                })
                                                                                )

                                                                )
                                                                .eventually(connection::close)
                                                )
                                )
                );
    }

    public Future<Void> markTaskAsFinished(final RoutingContext context, final MarkTaskAsFinishedRequest request) {
        return Future.succeededFuture(context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER))
                .compose(it -> {
                    if (it == null) {
                        return Future.failedFuture(new InternalServiceException(AppError.UNKNOWN_USER));
                    } else {
                        return Future.succeededFuture(it);
                    }
                })
                .compose(userJWTTokenInfo ->
                        pool.getConnection()
                                .compose(connection ->
                                        userService.validateUser(context, connection, userJWTTokenInfo.getUsername())
                                                .eventually(connection::close)
                                ).compose(userEntity ->
                                        pool.getConnection()
                                                .compose(connection ->
                                                        connection.begin().compose(transaction ->
                                                                        taskRepository.findTaskDetailOfUser(context, connection, BigInteger.valueOf(request.getId()), userEntity.getId())
                                                                                .compose(taskEntity -> {
                                                                                    if (taskEntity.isEmpty()) {
                                                                                        return Future.failedFuture(new InternalServiceException(AppError.TASK_NOT_FOUND));
                                                                                    }
                                                                                    final var task = taskEntity.get();
                                                                                    if (task.getDeletedAt() != null) {
                                                                                        return Future.failedFuture(new InternalServiceException(AppError.TASK_ALREADY_DELETED));
                                                                                    }
                                                                                    if (task.getFinished()) {
                                                                                        return Future.failedFuture(new InternalServiceException(AppError.TASK_ALREADY_FINISHED));
                                                                                    }
                                                                                    task.setUpdatedBy(userEntity.getUsername());
                                                                                    task.setUpdatedAt(DateTimeUtils.generateCurrentTimeDefault());
                                                                                    return taskRepository.update(context, connection, task);
                                                                                })
                                                                                .compose(it -> transaction.commit())
                                                                                .onFailure(
                                                                                        err -> transaction.rollback()
                                                                                                .onComplete(rollbackResult -> {
                                                                                                    if (rollbackResult.failed()) {
                                                                                                        err.addSuppressed(rollbackResult.cause());
                                                                                                    }
                                                                                                })
                                                                                )

                                                                )
                                                                .eventually(connection::close)
                                                )
                                )
                );
    }
}
