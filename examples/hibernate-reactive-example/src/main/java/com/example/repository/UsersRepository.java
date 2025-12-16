package com.example.repository;

import com.example.model.entity.UsersEntity;
import io.vertx.core.Future;
import org.hibernate.reactive.stage.Stage;
import vn.com.lcx.jpa.annotation.Param;
import vn.com.lcx.jpa.annotation.Query;
import vn.com.lcx.reactive.annotation.HRRepository;
import vn.com.lcx.reactive.repository.HReactiveRepository;

@HRRepository
public interface UsersRepository extends HReactiveRepository<UsersEntity> {

    @Query("from UsersEntity u where u.username = :username")
    Future<UsersEntity> findUser(Stage.Session session, @Param("username") String username);

}
