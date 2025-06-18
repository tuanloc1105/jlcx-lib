package com.example.lcx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.com.lcx.vertx.base.enums.ErrorCode;

@Getter
@AllArgsConstructor
public enum AppError implements ErrorCode {

    USER_EXISTED(400, 20001, "User already existed"),
    USER_NOT_EXIST(404, 20002, "User does not exist"),
    INACTIVE_USER(403, 20003, "The current user is not active"),
    UNKNOWN_USER(500, 20004, "Unknown user"),
    USER_IS_NOT_ACTIVE(403, 20005, "User has been locked"),
    INCORRECT_PASSWORD(400, 20006, "Incorrect password"),
    INVALID_REMIND_TIME(400, 20007, "Invalid remind time"),
    TASK_NOT_FOUND(404, 20008, "Task not found"),
    TASK_ALREADY_FINISHED(400, 20009, "Task already finished"),
    TASK_ALREADY_DELETED(400, 20010, "Task already deleted"),

    ;

    private final int httpCode;
    private final int code;
    private final String message;

}
