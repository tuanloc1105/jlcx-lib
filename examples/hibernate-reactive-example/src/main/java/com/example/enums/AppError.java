package com.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vn.com.lcx.vertx.base.enums.ErrorCode;

@AllArgsConstructor
@Getter
public enum AppError implements ErrorCode {

    DATA_EXISTED(404, 1001, "Data existed"),

    ;

    private final int httpCode;
    private final int code;
    private final String message;

}
