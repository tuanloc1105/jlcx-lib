package com.example.lcx.respository;

import com.example.lcx.entity.Task;
import vn.com.lcx.common.annotation.Repository;
import vn.com.lcx.common.database.repository.LCXRepository;

@Repository
public interface TaskRepository extends LCXRepository<Task> {
    Task findByIdAndFinishedAndCreatedBy(Long id, Boolean finished, String createdBy);

    Task findByIdAndCreatedBy(Long id, String createdBy);
}
