package com.example.lcx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.com.lcx.vertx.base.enums.ErrorCode;

@Getter
@AllArgsConstructor
public enum AppError implements ErrorCode {

    USER_EXISTED(400, 20001, "User already existed"),
    USER_NOT_EXIST(404, 20002, "User does not exist"),
    UNKNOWN_USER(500, 20002, "Unknown user"),
    USER_IS_NOT_ACTIVE(403, 20003, "User has been locked"),
    INCORRECT_PASSWORD(400, 20004, "Incorrect password"),
    INVALID_REMIND_TIME(400, 20005, "Invalid remind time"),
    TASK_NOT_FOUND(404, 20006, "Task not found"),
    TASK_ALREADY_FINISHED(400, 20007, "Task already finished"),

    ;

    private final int httpCode;
    private final int code;
    private final String message;

}
