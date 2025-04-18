package com.example.lcx.service.impl;

import com.example.lcx.dto.UserDTO;
import com.example.lcx.entity.User;
import com.example.lcx.http.request.CreateUserRequest;
import com.example.lcx.http.request.FindUserRequest;
import com.example.lcx.http.request.UpdateUserRequest;
import com.example.lcx.http.response.FindUserResponse;
import com.example.lcx.mapper.UserMapper;
import com.example.lcx.repository.UserRepository;
import com.example.lcx.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Service;
import vn.com.lcx.common.annotation.Transaction;
import vn.com.lcx.common.database.pageable.OraclePageable;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.PostgreSQLPageable;
import vn.com.lcx.common.database.specification.SimpleSpecificationImpl;
import vn.com.lcx.common.database.specification.Specification;

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

    public UserDTO findById(Long id) {
        val user = this.userRepository.findById(id);
        return this.userMapper.map(user);
    }

    public List<UserDTO> findAll(int pageNumber, int pageSize) {
        val users = this.userRepository.findAll(PostgreSQLPageable.builder().pageNumber(pageNumber).pageSize(pageSize).build());
        return users.stream()
                .map(this.userMapper::map)
                .collect(Collectors.toList());
    }

    public FindUserResponse findUser(FindUserRequest request) {
        Specification specification = SimpleSpecificationImpl.of(User.class);
        if (StringUtils.isNotBlank(request.getFirstName())) {
            specification.equal("firstName", request.getFirstName());
        }
        if (StringUtils.isNotBlank(request.getLastName())) {
            specification.and(
                    SimpleSpecificationImpl
                            .of(User.class)
                            .equal("lastName", request.getLastName())
            );
        }
        if (request.getAge() != null) {
            specification.and(
                    SimpleSpecificationImpl
                            .of(User.class)
                            .equal("age", request.getAge())
            );
        }
        Page<User> result = this.userRepository.find(
                specification,
                PostgreSQLPageable
                        .builder()
                        .pageSize(request.getPageSize() == null ? 10 : request.getPageSize())
                        .pageNumber(request.getPageNumber() == null ? 1 : request.getPageNumber())
                        .build()
        );
        return new FindUserResponse(result);
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
