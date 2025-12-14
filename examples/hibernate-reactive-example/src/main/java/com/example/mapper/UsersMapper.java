package com.example.mapper;

import com.example.model.dto.UsersDTO;
import com.example.model.entity.UsersEntity;
import vn.com.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface UsersMapper {

    UsersDTO map(UsersEntity entity);

}
