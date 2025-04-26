package com.example.lcx.mapper;

import com.example.lcx.entity.Task;
import com.example.lcx.object.dto.TaskDTO;
import com.example.lcx.object.request.CreateTaskRequest;
import vn.com.lcx.common.annotation.mapper.MapperClass;
import vn.com.lcx.common.annotation.mapper.Mapping;

@MapperClass
public interface TaskMapper {

    Task map(CreateTaskRequest request);

    TaskDTO map(Task task);

}
