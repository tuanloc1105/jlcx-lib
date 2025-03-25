package vn.com.lcx.vertx.base.enums;

import lombok.Getter;

@Getter
public enum ErrorCodeEnums implements ErrorCode {

    SUCCESS(200, 100000, "Success"),
    INVALID_REQUEST(400, 100001, "Invalid request"),
    INTERNAL_ERROR(500, 100002, "Internal error"),
    DATA_NOT_FOUND(404, 100003, "Data not found"),
    DATA_ERROR(400, 100004, "Data error"),

    ;

    private final int httpCode;
    private final int code;
    private final String message;

    ErrorCodeEnums(int httpCode, int code, String message) {
        this.httpCode = httpCode;
        this.code = code;
        this.message = message;
    }

}
