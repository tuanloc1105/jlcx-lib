package com.example.lcx.respository.reactive;

import com.example.lcx.entity.reactive.TaskEntity;
import vn.com.lcx.reactive.annotation.RRepository;
import vn.com.lcx.reactive.repository.ReactiveRepository;

@RRepository
public interface TaskRepository extends ReactiveRepository<TaskEntity> {
}
