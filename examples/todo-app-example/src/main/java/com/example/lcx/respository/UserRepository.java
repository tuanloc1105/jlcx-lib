package com.example.lcx.respository;

import com.example.lcx.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlConnection;
import vn.com.lcx.reactive.annotation.Query;
import vn.com.lcx.reactive.annotation.RRepository;
import vn.com.lcx.reactive.repository.ReactiveRepository;

import java.util.Optional;

@RRepository
public interface UserRepository extends ReactiveRepository<UserEntity> {

    @Query("select * from r_lcx.user where username = ?")
    Future<Optional<UserEntity>> findByUsername(RoutingContext context, SqlConnection client, String username);

}
