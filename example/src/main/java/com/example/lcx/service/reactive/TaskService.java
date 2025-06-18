package com.example.lcx.service.reactive;

import com.example.lcx.entity.reactive.TaskEntity;
import com.example.lcx.mapper.TaskMapper;
import com.example.lcx.object.dto.ReactiveTaskDTO;
import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateTaskRequest;
import com.example.lcx.object.request.GetTaskDetailRequest;
import com.example.lcx.object.request.SearchTasksByNameRequest;
import com.example.lcx.respository.reactive.TaskRepository;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.pageable.Page;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class TaskService {
    private final Pool pool;
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public Future<Void> createTask(final RoutingContext context, final CreateTaskRequest request) {
        return Future.<UserJWTTokenInfo>future(
                        event -> context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER)
                )
                .compose(userJWTTokenInfo ->
                        pool.getConnection().compose(sqlConnection ->
                                userService.validateUser(context, sqlConnection, userJWTTokenInfo.getUsername())
                                        .compose(userEntity ->
                                                sqlConnection.begin()
                                                        .compose(transaction -> {
                                                            final TaskEntity newTask = taskMapper.mapToReactiveEntity(request);
                                                            newTask.setUserId(userEntity.getId());
                                                            newTask.setCreatedBy(userEntity.getUsername());
                                                            newTask.setUpdatedBy(userEntity.getUsername());
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
        return Future.<UserJWTTokenInfo>future(
                        event -> context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER)
                )
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

    // public Future<Page<ReactiveTaskDTO>> searchTasksByName(final RoutingContext context, final SearchTasksByNameRequest request) {
    //     return Future.<UserJWTTokenInfo>future(
    //                     event -> context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER)
    //             )
    //             .compose(userJWTTokenInfo ->
    //                     pool.getConnection()
    //                             .compose(sqlConnection ->
    //                                     userService.validateUser(context, sqlConnection, userJWTTokenInfo.getUsername())
    //                                             .compose(userEntity ->
    //                                                     taskRepository.searchTaskOfUser(
    //                                                             context,
    //                                                             sqlConnection,
    //                                                             request.getSearchContent(),
    //                                                             userEntity.getId()
    //                                                     ).compose(taskEntity ->
    //                                                             taskEntity
    //                                                                     .map(it -> Future.succeededFuture(taskMapper.mapToReactiveDTO(it)))
    //                                                                     .orElse(Future.succeededFuture((new ReactiveTaskDTO())))
    //                                                     )
    //                                             )
    //                                             .eventually(sqlConnection::close)
    //                             )
    //             );
    // }

}
