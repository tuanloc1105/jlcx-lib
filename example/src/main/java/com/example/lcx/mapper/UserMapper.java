package com.example.lcx.mapper;

import com.example.lcx.dto.UserDTO;
import com.example.lcx.entity.User;
import com.example.lcx.http.request.CreateUserRequest;
import vn.com.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface UserMapper {

    User map(UserDTO dto);

    User map(CreateUserRequest dto);

    UserDTO map(User user);

}
