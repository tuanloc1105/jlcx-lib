package com.example.lcx.mapper;

import com.example.lcx.entity.TaskEntity;
import com.example.lcx.object.dto.ReactiveTaskDTO;
import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateTaskRequest;
import vn.io.lcx.common.annotation.mapper.MapperClass;
import vn.io.lcx.common.annotation.mapper.Mapping;

@MapperClass
public interface TaskMapper {

    TaskEntity mapToReactiveEntity(CreateTaskRequest request);

    @Mapping(fromParameter = "userInfo", fromField = "username", toField = "createdBy")
    @Mapping(toField = "finished", code = "false")
    @Mapping(toField = "updatedBy", skip = true)
    TaskEntity mapToReactiveEntity(CreateTaskRequest request, UserJWTTokenInfo userInfo);

    ReactiveTaskDTO mapToReactiveDTO(TaskEntity task);

}
