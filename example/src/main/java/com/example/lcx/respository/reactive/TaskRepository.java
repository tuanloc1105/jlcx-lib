package com.example.lcx.respository.reactive;

import com.example.lcx.entity.reactive.TaskEntity;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlConnection;
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
            "    t.id = $1\n" +
            "    AND t.finished = $2\n" +
            "    AND t.user_id = $3")
    Future<TaskEntity> findByIdAndFinishedOfUser(RoutingContext context, SqlConnection client, BigInteger id, Boolean finished, BigInteger userId);

    @Query("SELECT\n" +
            "    t.*\n" +
            "FROM\n" +
            "    r_lcx.task t\n" +
            "    LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE\n" +
            "    AND t.user_id = $1")
    Future<List<TaskEntity>> findTasksOfUser(RoutingContext context, SqlConnection client, BigInteger userId);

    @Query("SELECT\n" +
            "    t.*\n" +
            "FROM\n" +
            "    r_lcx.task t\n" +
            "    LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE\n" +
            "    t.id = $1\n" +
            "    AND t.user_id = $2")
    Future<Optional<TaskEntity>> findTaskDetailOfUser(RoutingContext context, SqlConnection client, BigInteger id, BigInteger userId);

}
