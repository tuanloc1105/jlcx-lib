package com.example.service;

import com.example.mapper.TaskMapper;
import com.example.model.dto.TaskDTO;
import com.example.model.dto.UserJWTTokenInfo;
import com.example.model.entity.TasksEntity;
import com.example.model.enums.AppError;
import com.example.model.http.request.CreateTaskRequest;
import com.example.model.http.request.DeleteTaskRequest;
import com.example.model.http.request.GetAllTaskRequest;
import com.example.model.http.request.GetTaskDetailRequest;
import com.example.model.http.request.SearchTasksByNameRequest;
import com.example.model.http.request.UpdateTaskRequest;
import com.example.repository.TasksRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.hibernate.reactive.stage.Stage;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.database.pageable.Direction;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.jpa.respository.CriteriaHandler;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TasksService {

    private final Stage.SessionFactory sessionFactory;
    private final TasksRepository tasksRepository;
    private final UsersService usersService;
    private final TaskMapper taskMapper;

    private static Future<UserJWTTokenInfo> getUserFromContext(final RoutingContext context) {
        final var promise = Promise.<UserJWTTokenInfo>promise();
        final var userInfo = context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER);
        if (userInfo == null) {
            promise.fail(new InternalServiceException(AppError.UNKNOWN_USER));
        }
        promise.succeed(userInfo);
        return promise.future();
    }

    public Future<Void> createTask(final RoutingContext context, final CreateTaskRequest request) {
        final var validateUserFuture = getUserFromContext(context)
                .compose(userJWTTokenInfo -> usersService.validateUser(userJWTTokenInfo.getUsername()));
        return validateUserFuture.compose(user ->
                Future.fromCompletionStage(
                        sessionFactory.withTransaction((session, transaction) ->
                                {
                                    final var entity = new TasksEntity();
                                    entity.setUser(user);
                                    entity.setTaskTitle(request.getTaskTitle());
                                    entity.setTaskDetail(request.getTaskDetail());
                                    final var future = tasksRepository.save(session, entity)
                                            .onFailure(e ->
                                                    {
                                                        LogUtils.writeLog(context, e.getMessage(), e);
                                                        transaction.markForRollback();
                                                    }
                                            );
                                    return future.toCompletionStage();
                                }
                        )
                )
        ).map(CommonConstant.VOID);
    }

    public Future<TaskDTO> getTaskDetail(final RoutingContext context, final GetTaskDetailRequest request) {
        final var validateUserFuture = getUserFromContext(context)
                .compose(userJWTTokenInfo -> usersService.validateUser(userJWTTokenInfo.getUsername()));
        return validateUserFuture.compose(user ->
                Future.fromCompletionStage(
                        sessionFactory.withSession(session ->
                                {
                                    final var varfindTaskDetailFuture = tasksRepository.findTaskDetail(
                                            session,
                                            user,
                                            request.getId()
                                    ).map(opt ->
                                            {
                                                if (opt.isEmpty()) {
                                                    throw new InternalServiceException(AppError.TASK_NOT_FOUND);
                                                }
                                                return taskMapper.map(opt.get());
                                            }
                                    );
                                    return varfindTaskDetailFuture.toCompletionStage();
                                }
                        )
                )
        );
    }

    public Future<Page<TaskDTO>> searchTaskByName(final RoutingContext context, final SearchTasksByNameRequest request) {
        final var validateUserFuture = getUserFromContext(context)
                .compose(userJWTTokenInfo -> usersService.validateUser(userJWTTokenInfo.getUsername()));
        return validateUserFuture.compose(user ->
                Future.fromCompletionStage(
                        sessionFactory.withSession(session ->
                                {
                                    CriteriaHandler<TasksEntity> criteriaHandler = (cb, cq, root) -> {
                                        List<Predicate> predicates = new ArrayList<>();
                                        predicates.add(
                                                cb.equal(root.get("user"), user)
                                        );
                                        predicates.add(
                                                cb.or(
                                                        cb.like(root.get("taskTitle"), "%" + request.getSearchContent() + "%"),
                                                        cb.like(root.get("taskDetail"), "%" + request.getSearchContent() + "%")
                                                )
                                        );
                                        predicates.add(
                                                cb.isNull(root.get("deletedAt"))
                                        );
                                        return cb.and(predicates.toArray(Predicate[]::new));
                                    };
                                    final var searchTaskFuture = tasksRepository.find(
                                            session,
                                            criteriaHandler,
                                            Pageable.ofPageable(request.getPageNumber(), 10)
                                                    .add("id", Direction.DESC)
                                    ).map(page -> Page.create(page, taskMapper::map));
                                    return searchTaskFuture.toCompletionStage();
                                }
                        )
                )
        );
    }

    public Future<Page<TaskDTO>> getAllTask(final RoutingContext context, final GetAllTaskRequest request) {
        final var validateUserFuture = getUserFromContext(context)
                .compose(userJWTTokenInfo -> usersService.validateUser(userJWTTokenInfo.getUsername()));
        return validateUserFuture.compose(user ->
                Future.fromCompletionStage(
                        sessionFactory.withSession(session ->
                                {
                                    // CriteriaHandler<TasksEntity> criteriaHandler = (cb, cq, root) ->
                                    //         cb.conjunction();
                                    CriteriaHandler<TasksEntity> criteriaHandler = (cb, cq, root) -> {
                                        List<Predicate> predicates = new ArrayList<>();
                                        predicates.add(
                                                cb.equal(root.get("user"), user)
                                        );
                                        predicates.add(
                                                cb.isNull(root.get("deletedAt"))
                                        );
                                        return cb.and(predicates.toArray(Predicate[]::new));
                                    };
                                    final var searchTaskFuture = tasksRepository.find(
                                            session,
                                            criteriaHandler,
                                            Pageable.ofPageable(request.getPageNumber(), 10)
                                                    .add("id", Direction.DESC)
                                    ).map(page -> Page.create(page, taskMapper::map));
                                    return searchTaskFuture.toCompletionStage();
                                }
                        )
                )
        );
    }

    public Future<Void> updateTask(final RoutingContext context, final UpdateTaskRequest request) {
        final var validateUserFuture = getUserFromContext(context)
                .compose(userJWTTokenInfo -> usersService.validateUser(userJWTTokenInfo.getUsername()));
        return validateUserFuture.compose(user ->
                Future.fromCompletionStage(
                        sessionFactory.withTransaction((session, transaction) ->
                                {
                                    final var updateTaskFuture = tasksRepository.findTaskDetail(
                                            session,
                                            user,
                                            request.getId()
                                    ).compose(opt ->
                                            {
                                                if (opt.isEmpty()) {
                                                    return Future.failedFuture(new InternalServiceException(AppError.TASK_NOT_FOUND));
                                                }
                                                final var entity = opt.get();
                                                entity.setTaskDetail(request.getTaskDetail());
                                                entity.setTaskTitle(request.getTaskTitle());
                                                if (request.getFinished() != null) {
                                                    entity.setFinished(request.getFinished());
                                                }
                                                return tasksRepository.save(session, entity)
                                                        .map(CommonConstant.VOID);
                                            }
                                    );
                                    return updateTaskFuture.toCompletionStage();
                                }
                        )
                )
        );
    }

    public Future<Void> deleteTask(final RoutingContext context, final DeleteTaskRequest request) {
        final var validateUserFuture = getUserFromContext(context)
                .compose(userJWTTokenInfo -> usersService.validateUser(userJWTTokenInfo.getUsername()));
        return validateUserFuture.compose(user ->
                Future.fromCompletionStage(
                        sessionFactory.withTransaction((session, transaction) ->
                                {
                                    final var deleteTaskFuture = tasksRepository.findTaskDetail(
                                            session,
                                            user,
                                            request.getId()
                                    ).compose(opt ->
                                            {
                                                if (opt.isEmpty()) {
                                                    return Future.failedFuture(new InternalServiceException(AppError.TASK_NOT_FOUND));
                                                }
                                                final var entity = opt.get();
                                                entity.setDeletedAt(DateTimeUtils.generateCurrentTimeDefault());
                                                return tasksRepository.delete(session, entity);
                                            }
                                    ).map(CommonConstant.VOID);
                                    return deleteTaskFuture.toCompletionStage();
                                }
                        )
                )
        );
    }

}
