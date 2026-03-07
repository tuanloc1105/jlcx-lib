package com.example.repository;

import com.example.model.entity.UsersEntity;
import vn.io.lcx.reactive.annotation.HRRepository;
import vn.io.lcx.reactive.repository.HReactiveRepository;

@HRRepository
public interface UsersRepository extends HReactiveRepository<UsersEntity> {
}
