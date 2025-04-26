package com.example.lcx.respository;

import com.example.lcx.entity.User;
import vn.com.lcx.common.annotation.Repository;
import vn.com.lcx.common.database.repository.LCXRepository;

@Repository
public interface UserRepository extends LCXRepository<User> {
}
