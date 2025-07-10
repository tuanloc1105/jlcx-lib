package com.example.lcx.respository.reactive;

import com.example.lcx.entity.reactive.TaskEntity;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlConnection;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.reactive.annotation.Query;
import vn.com.lcx.reactive.annotation.RRepository;
import vn.com.lcx.reactive.repository.ReactiveRepository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@RRepository
public interface TaskRepository extends ReactiveRepository<TaskEntity> {

    Future<Optional<TaskEntity>> findByIdAndFinished(RoutingContext context, SqlConnection client, BigInteger id, Boolean finished);

    @Query("SELECT\n" +
            "    t.*\n" +
            "FROM\n" +
            "    r_lcx.task t\n" +
            "    LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE\n" +
            "    t.id = ?\n" +
            "    AND t.user_id = ?")
    Future<Optional<TaskEntity>> findTaskDetailOfUser(RoutingContext context, SqlConnection client, BigInteger id, BigInteger userId);

    @Query("SELECT\n" +
            "    t.*\n" +
            "FROM\n" +
            "    r_lcx.task t\n" +
            "    LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE\n" +
            "    t.task_name ilike ?\n" +
            "    AND t.deleted_at is null\n" +
            "    AND t.user_id = ?")
    Future<List<TaskEntity>> searchTaskOfUser(RoutingContext context, SqlConnection client, String taskName, BigInteger userId, Pageable pageable);

    @Query("SELECT\n" +
            "    COUNT(1)\n" +
            "FROM\n" +
            "    r_lcx.task t\n" +
            "    LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE\n" +
            "    t.task_name ilike ?\n" +
            "    AND t.deleted_at is null\n" +
            "    AND t.user_id = ?")
    Future<Long> countSearchTaskOfUser(RoutingContext context, SqlConnection client, String taskName, BigInteger userId);

    @Query("SELECT\n" +
            "    t.*\n" +
            "FROM\n" +
            "    r_lcx.task t\n" +
            "    LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE\n" +
            "    t.deleted_at is null\n" +
            "    AND t.user_id = ?")
    Future<List<TaskEntity>> getTasksOfUser(RoutingContext context, SqlConnection client, BigInteger userId, Pageable pageable);

    @Query("SELECT\n" +
            "    COUNT(1)\n" +
            "FROM\n" +
            "    r_lcx.task t\n" +
            "    LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE\n" +
            "    t.deleted_at is null\n" +
            "    AND t.user_id = ?")
    Future<Long> countTasksOfUser(RoutingContext context, SqlConnection client, BigInteger userId);

}
