package com.example.repository;

import com.example.model.entity.TasksEntity;
import com.example.model.entity.UsersEntity;
import io.vertx.core.Future;
import org.hibernate.reactive.stage.Stage;
import vn.com.lcx.jpa.annotation.Query;
import vn.com.lcx.reactive.annotation.HRRepository;
import vn.com.lcx.reactive.repository.HReactiveRepository;

import java.util.Optional;

@HRRepository
public interface TasksRepository extends HReactiveRepository<TasksEntity> {

    @Query("from TasksEntity t where t.user = ?1 and t.id = ?2")
    Future<Optional<TasksEntity>> findTaskDetail(Stage.Session session, UsersEntity user, Long id);

}
