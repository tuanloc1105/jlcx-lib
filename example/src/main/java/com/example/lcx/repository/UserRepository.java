package com.example.lcx.repository;

import com.example.lcx.entity.User;
import vn.com.lcx.common.annotation.Modifying;
import vn.com.lcx.common.annotation.Query;
import vn.com.lcx.common.annotation.Repository;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.common.database.repository.LCXRepository;

import java.util.List;

@Repository
public interface UserRepository extends LCXRepository<User> {

    User findById(Long id);

    List<User> findAll(Pageable pageable);

    @Query(nativeQuery = "SELECT\n    u.id AS U_id,\n    u.first_name AS U_first_name,\n    u.last_name AS U_last_name,\n    u.age AS U_age\nFROM\n    public.user u\nWHERE\n    u.age = ? and u.id in :idList")
    List<User> testFind(int age, List<Long> idList);

    @Query(nativeQuery = "DELETE FROM public.user WHERE id = ?")
    @Modifying
    int deleteTest(Long id);

}
