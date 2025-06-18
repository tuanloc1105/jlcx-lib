package com.example.lcx.mapper;

import com.example.lcx.entity.TaskEntity;
import com.example.lcx.object.dto.ReactiveTaskDTO;
import com.example.lcx.object.dto.TaskDTO;
import com.example.lcx.object.request.CreateTaskRequest;
import vn.com.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface TaskMapper {

    TaskEntity map(CreateTaskRequest request);

    com.example.lcx.entity.reactive.TaskEntity mapToReactiveEntity(CreateTaskRequest request);

    TaskDTO map(TaskEntity task);

    ReactiveTaskDTO mapToReactiveDTO(com.example.lcx.entity.reactive.TaskEntity task);

}
