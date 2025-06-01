package com.example.lcx.respository;

import com.example.lcx.entity.TaskEntity;
import vn.com.lcx.jpa.respository.JpaRepository;

import java.math.BigInteger;

public interface TaskRepo extends JpaRepository<TaskEntity, BigInteger> {
}
