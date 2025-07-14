package com.example.lcx.mapper;

import com.example.lcx.entity.TaskEntity;
import com.example.lcx.object.dto.ReactiveTaskDTO;
import com.example.lcx.object.request.CreateTaskRequest;
import vn.com.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface TaskMapper {

    TaskEntity mapToReactiveEntity(CreateTaskRequest request);

    ReactiveTaskDTO mapToReactiveDTO(TaskEntity task);

}
