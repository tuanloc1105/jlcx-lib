package com.example.lcx.respository.reactive;

import com.example.lcx.entity.reactive.TaskEntity;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import vn.com.lcx.reactive.annotation.Query;
import vn.com.lcx.reactive.annotation.RRepository;
import vn.com.lcx.reactive.repository.ReactiveRepository;

import java.math.BigInteger;

@RRepository
public interface TaskRepository extends ReactiveRepository<TaskEntity> {

    Future<RowSet<Row>> findByIdAndFinished(RoutingContext context, SqlConnection client, BigInteger id, Boolean finished);

    @Query("SELECT\n" +
            "    t.*\n" +
            "FROM\n" +
            "    r_lcx.task t\n" +
            "    LEFT JOIN r_lcx.user u ON t.user_id = u.id\n" +
            "WHERE\n" +
            "    t.id = $1\n" +
            "    AND t.finished = $2\n" +
            "    AND t.user_id = $3")
    Future<RowSet<Row>> findByIdAndFinishedOfUser(RoutingContext context, SqlConnection client, BigInteger id, Boolean finished, BigInteger userId);

}
