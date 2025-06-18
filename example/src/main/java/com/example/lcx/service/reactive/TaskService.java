package com.example.lcx.service.reactive;

import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.example.lcx.object.request.CreateTaskRequest;
import com.example.lcx.respository.reactive.TaskRepository;
import com.example.lcx.respository.reactive.UserRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;

@Component
@RequiredArgsConstructor
public class TaskService {
    private final Pool pool;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public Future<Void> createTask(final RoutingContext context, final CreateTaskRequest request) {
        Promise<Void> promise = Promise.promise();
        final var currentUser = context.<UserJWTTokenInfo>get(CommonConstant.CURRENT_USER);
        return promise.future();
    }

}
