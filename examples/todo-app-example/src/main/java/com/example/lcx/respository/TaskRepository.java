package com.example.lcx.respository;

import com.example.lcx.entity.TaskEntity;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlConnection;
import vn.com.lcx.common.database.pageable.Page;
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

    @Query("SELECT t.*\n" +
            "FROM r_lcx.task t\n" +
            "LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE t.id = ?\n" +
            "    AND t.user_id = ?")
    Future<Optional<TaskEntity>> findTaskDetailOfUser(RoutingContext context, SqlConnection client, BigInteger id, BigInteger userId);

    @Query("SELECT t.*\n" +
            "FROM r_lcx.task t\n" +
            "LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE t.task_name ILIKE ?\n" +
            "    AND t.deleted_at IS NULL\n" +
            "    AND t.user_id = ?")
    Future<List<TaskEntity>> searchTaskOfUser(RoutingContext context, SqlConnection client, String taskName, BigInteger userId, Pageable pageable);

    @Query("SELECT t.*\n" +
            "FROM r_lcx.task t\n" +
            "LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE t.task_name ILIKE ?\n" +
            "    AND t.deleted_at IS NULL\n" +
            "    AND t.user_id = ?")
    Future<Page<TaskEntity>> searchTaskOfUserPage(RoutingContext context, SqlConnection client, String taskName, BigInteger userId, Pageable pageable);

    @Query("SELECT count(1)\n" +
            "FROM r_lcx.task t\n" +
            "LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE t.task_name ILIKE ?\n" +
            "    AND t.deleted_at IS NULL\n" +
            "    AND t.user_id = ?")
    Future<Long> countSearchTaskOfUser(RoutingContext context, SqlConnection client, String taskName, BigInteger userId);

    @Query("SELECT t.*\n" +
            "FROM r_lcx.task t\n" +
            "LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE t.deleted_at IS NULL\n" +
            "    AND t.user_id = ?")
    Future<List<TaskEntity>> getTasksOfUser(RoutingContext context, SqlConnection client, BigInteger userId, Pageable pageable);

    @Query("SELECT count(1)\n" +
            "FROM r_lcx.task t\n" +
            "LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE t.deleted_at IS NULL\n" +
            "    AND t.user_id = ?")
    Future<Long> countTasksOfUser(RoutingContext context, SqlConnection client, BigInteger userId);

}
