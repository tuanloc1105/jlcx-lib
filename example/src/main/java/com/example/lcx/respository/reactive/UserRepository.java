package com.example.lcx.respository.reactive;

import com.example.lcx.entity.reactive.UserEntity;
import vn.com.lcx.reactive.annotation.RRepository;
import vn.com.lcx.reactive.repository.ReactiveRepository;

@RRepository
public interface UserRepository extends ReactiveRepository<UserEntity> {
}
