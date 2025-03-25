package com.example.lcx.service.impl;

import com.example.lcx.dto.UserDTO;
import com.example.lcx.http.request.CreateUserRequest;
import com.example.lcx.http.request.UpdateUserRequest;
import com.example.lcx.mapper.UserMapper;
import com.example.lcx.repository.UserRepository;
import com.example.lcx.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Service;
import vn.com.lcx.common.annotation.Transaction;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transaction
    public UserDTO save(CreateUserRequest request) {
        val user = this.userMapper.map(request);
        this.userRepository.save2(user);
        return this.userMapper.map(user);
    }

    @Transaction(readOnly = true)
    public UserDTO findById(Long id) {
        val user = this.userRepository.findById(id);
        return this.userMapper.map(user);
    }

    @Transaction(readOnly = true)
    public List<UserDTO> findAll() {
        val users = this.userRepository.findAll();
        return users.stream()
                .map(this.userMapper::map)
                .collect(Collectors.toList());
    }

    @Transaction
    public void deleteById(Long id) {
        val user = this.userRepository.findById(id);
        if (user.getId() == null) {
            throw new RuntimeException("Cannot find user");
        }
        this.userRepository.delete(user);
    }

    @Transaction
    public UserDTO update(UpdateUserRequest request) {
        val user = this.userRepository.findById(request.getId());
        if (user.getId() == null) {
            throw new RuntimeException("Cannot find user");
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        this.userRepository.update(user);
        return this.userMapper.map(user);
    }

}
