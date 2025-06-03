package com.example.lcx.service.impl;

import com.example.lcx.entity.TaskEntity;
import com.example.lcx.entity.UserEntity;
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
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.database.pageable.Direction;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.common.utils.DateTimeUtils;
import vn.com.lcx.jpa.annotation.Service;
import vn.com.lcx.jpa.annotation.Transactional;
import vn.com.lcx.vertx.base.context.AuthContext;
import vn.com.lcx.vertx.base.exception.InternalServiceException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
@Component
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;

    @Transactional(onRollback = {Exception.class})
    public void createTask(final CreateTaskRequest request) {
        final var currentDateTime = DateTimeUtils.generateCurrentTimeDefault();
        if (request.getRemindAt().isBefore(currentDateTime)) {
            throw new InternalServiceException(AppError.INVALID_REMIND_TIME);
        }
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var user = userService.getUserByUsername(currentUser.getUsername())
                .orElseThrow(() -> new InternalServiceException(AppError.USER_NOT_EXIST));
        final var newTask = taskMapper.map(request);
        newTask.setUser(user);
        newTask.setCreatedBy(currentUser.getUsername());
        newTask.setUpdatedBy(currentUser.getUsername());
        taskRepository.save(newTask);
    }

    public TaskDTO getTaskDetail(final GetTaskDetailRequest request) {
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var user = userService.getUserByUsername(currentUser.getUsername())
                .orElseThrow(() -> new InternalServiceException(AppError.USER_NOT_EXIST));
        return taskMapper.map(
                taskRepository.findOne(
                        (cb, cq, root) -> {
                            Join<TaskEntity, UserEntity> joinUser = root.join("user", JoinType.LEFT);
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(cb.equal(root.get("id"), BigInteger.valueOf(request.getId())));
                            predicates.add(cb.equal(root.get("finished"), false));
                            predicates.add(cb.equal(joinUser.get("id"), user.getId()));
                            return cb.and(predicates.toArray(Predicate[]::new));
                        }
                ).orElse(new TaskEntity())
        );
    }

    public Page<TaskDTO> searchTasksByName(final SearchTasksByNameRequest request) {
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var user = userService.getUserByUsername(currentUser.getUsername())
                .orElseThrow(() -> new InternalServiceException(AppError.USER_NOT_EXIST));
        final var searchResult = taskRepository.find(
                (cb, cq, root) -> {
                    Join<TaskEntity, UserEntity> joinUser = root.join("user", JoinType.LEFT);
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.like(root.get("taskName"), request.getSearchContent()));
                    predicates.add(cb.isNull(root.get("deletedAt")));
                    predicates.add(cb.equal(joinUser.get("id"), user.getId()));
                    return cb.and(predicates.toArray(Predicate[]::new));
                },
                Pageable.ofPageable(request.getPageNumber(), 10).add("id", Direction.DESC)
        );
        return Page.create(searchResult, taskMapper::map);
    }

    @Transactional
    public Page<TaskDTO> getAllTask(final GetAllTaskRequest request) {
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var user = userService.getUserByUsername(currentUser.getUsername())
                .orElseThrow(() -> new InternalServiceException(AppError.USER_NOT_EXIST));
        final var searchResult = taskRepository.find(
                (cb, cq, root) -> {
                    Join<TaskEntity, UserEntity> joinUser = root.join("user", JoinType.LEFT);
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isNull(root.get("deletedAt")));
                    predicates.add(cb.equal(joinUser.get("id"), user.getId()));
                    return cb.and(predicates.toArray(Predicate[]::new));
                },
                Pageable.ofPageable(request.getPageNumber(), 10).add("id", Direction.DESC)
        );
        return Page.create(searchResult, taskMapper::map);
    }

    @Transactional(onRollback = {Exception.class})
    public void updateTask(final UpdateTaskRequest request) {
        final var currentDateTime = DateTimeUtils.generateCurrentTimeDefault();
        if (request.getRemindAt().isBefore(currentDateTime)) {
            throw new InternalServiceException(AppError.INVALID_REMIND_TIME);
        }
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var user = userService.getUserByUsername(currentUser.getUsername())
                .orElseThrow(() -> new InternalServiceException(AppError.USER_NOT_EXIST));
        final var task = taskRepository.findOne(
                (cb, cq, root) -> {
                    Join<TaskEntity, UserEntity> joinUser = root.join("user", JoinType.LEFT);
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isNull(root.get("deletedAt")));
                    predicates.add(cb.equal(root.get("id"), BigInteger.valueOf(request.getId())));
                    predicates.add(cb.equal(joinUser.get("id"), user.getId()));
                    return cb.and(predicates.toArray(Predicate[]::new));

                }
        ).orElseThrow(() -> new InternalServiceException(AppError.TASK_NOT_FOUND));
        task.setTaskName(request.getTaskName());
        task.setTaskDetail(request.getTaskDetail());
        task.setRemindAt(request.getRemindAt());
        task.setUpdatedBy(currentUser.getUsername());
        task.setUpdatedAt(currentDateTime);
        taskRepository.update(task);
    }

    @Transactional(onRollback = {Exception.class})
    public void deleteTask(final DeleteTaskRequest request) {
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var user = userService.getUserByUsername(currentUser.getUsername())
                .orElseThrow(() -> new InternalServiceException(AppError.USER_NOT_EXIST));
        final var task = taskRepository.findOne(
                (cb, cq, root) -> {
                    Join<TaskEntity, UserEntity> joinUser = root.join("user", JoinType.LEFT);
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isNull(root.get("deletedAt")));
                    predicates.add(cb.equal(root.get("id"), BigInteger.valueOf(request.getId())));
                    predicates.add(cb.equal(joinUser.get("id"), user.getId()));
                    return cb.and(predicates.toArray(Predicate[]::new));

                }
        ).orElseThrow(() -> new InternalServiceException(AppError.TASK_NOT_FOUND));
        task.setDeletedAt(DateTimeUtils.generateCurrentTimeDefault());
        taskRepository.update(task);
    }

    @Transactional(onRollback = {Exception.class})
    public void markTaskAsFinished(final MarkTaskAsFinishedRequest request) {
        final var currentUser = (UserJWTTokenInfo) AuthContext.get();
        final var user = userService.getUserByUsername(currentUser.getUsername())
                .orElseThrow(() -> new InternalServiceException(AppError.USER_NOT_EXIST));
        final var task = taskRepository.findOne(
                (cb, cq, root) -> {
                    Join<TaskEntity, UserEntity> joinUser = root.join("user", JoinType.LEFT);
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isNull(root.get("deletedAt")));
                    predicates.add(cb.equal(root.get("id"), BigInteger.valueOf(request.getId())));
                    predicates.add(cb.equal(joinUser.get("id"), user.getId()));
                    return cb.and(predicates.toArray(Predicate[]::new));

                }
        ).orElseThrow(() -> new InternalServiceException(AppError.TASK_NOT_FOUND));
        task.setFinished(true);
        taskRepository.update(task);
    }

}
