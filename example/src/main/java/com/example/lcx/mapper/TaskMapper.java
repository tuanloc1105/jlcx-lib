package com.example.lcx.mapper;

import com.example.lcx.entity.TaskEntity;
import com.example.lcx.object.dto.TaskDTO;
import com.example.lcx.object.request.CreateTaskRequest;
import vn.com.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface TaskMapper {

    TaskEntity map(CreateTaskRequest request);

    TaskDTO map(TaskEntity task);

}
