package com.example.repository;

import com.example.model.entity.TasksEntity;
import vn.com.lcx.reactive.annotation.HRRepository;
import vn.com.lcx.reactive.repository.HReactiveRepository;

@HRRepository
public interface TasksRepository extends HReactiveRepository<TasksEntity> {
}
