package com.example.mapper;

import com.example.model.dto.TaskDTO;
import com.example.model.entity.TasksEntity;
import vn.io.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface TaskMapper {

    TaskDTO map(TasksEntity entity);

}
