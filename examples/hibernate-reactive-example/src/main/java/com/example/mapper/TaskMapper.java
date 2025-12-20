package com.example.mapper;

import com.example.model.dto.TaskDTO;
import com.example.model.entity.TasksEntity;
import vn.com.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface TaskMapper {

    TaskDTO map(TasksEntity entity);

}
