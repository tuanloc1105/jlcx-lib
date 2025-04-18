package com.example.lcx.service;

import com.example.lcx.dto.UserDTO;
import com.example.lcx.http.request.CreateUserRequest;
import com.example.lcx.http.request.FindUserRequest;
import com.example.lcx.http.request.UpdateUserRequest;
import com.example.lcx.http.response.FindUserResponse;

import java.util.List;

public interface UserService {
    UserDTO save(CreateUserRequest request);

    UserDTO findById(Long id);

    List<UserDTO> findAll(int pageNumber, int pageSize);

    FindUserResponse findUser(FindUserRequest request);

    void deleteById(Long id);

    UserDTO update(UpdateUserRequest request);
}
