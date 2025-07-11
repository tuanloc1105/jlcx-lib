package com.example.lcx.mapper;

import com.example.lcx.object.dto.ReactiveTaskDTO;
import com.example.lcx.object.request.CreateTaskRequest;
import vn.com.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface TaskMapper {

    com.example.lcx.entity.reactive.TaskEntity mapToReactiveEntity(CreateTaskRequest request);

    ReactiveTaskDTO mapToReactiveDTO(com.example.lcx.entity.reactive.TaskEntity task);

}
