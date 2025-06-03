package com.example.lcx.respository;

import com.example.lcx.entity.TaskEntity;
import vn.com.lcx.jpa.annotation.Repository;
import vn.com.lcx.jpa.respository.JpaRepository;

import java.math.BigInteger;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, BigInteger> {
}
