package com.example.lcx.mapper;

import com.example.lcx.entity.User;
import com.example.lcx.entity.UserEntity;
import com.example.lcx.object.dto.UserDTO;
import vn.com.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface UserMapper {

    public UserDTO map(final User user);

    public UserDTO map(final UserEntity user);

}
