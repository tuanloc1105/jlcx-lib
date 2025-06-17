package com.example.lcx.respository.reactive;

import com.example.lcx.entity.reactive.UserEntity;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import vn.com.lcx.reactive.annotation.RRepository;
import vn.com.lcx.reactive.repository.ReactiveRepository;

import java.math.BigInteger;

@RRepository
public interface UserRepository extends ReactiveRepository<UserEntity> {

    Future<RowSet<Row>> findByUsername(RoutingContext context, SqlConnection client, String username);

}
