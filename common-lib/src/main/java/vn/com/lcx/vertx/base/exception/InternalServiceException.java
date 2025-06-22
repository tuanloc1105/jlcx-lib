package vn.com.lcx.vertx.base.exception;

import vn.com.lcx.vertx.base.enums.ErrorCode;
import vn.com.lcx.vertx.base.enums.ErrorCodeEnums;

public class InternalServiceException extends RuntimeException {
    private static final long serialVersionUID = -615514778045395314L;

    private int httpCode;
    private int code;
    private String message;

    public InternalServiceException() {
        this(ErrorCodeEnums.INTERNAL_ERROR);
    }

    public InternalServiceException(int httpCode, int code, String message) {
        super(
                String.format(
                        "%d - %d - %s",
                        httpCode,
                        code,
                        message
                )
        );
        this.httpCode = httpCode;
        this.code = code;
        this.message = message;
    }

    public InternalServiceException(int httpCode, int code, String message, String... additionalMessage) {
        this(
                httpCode,
                code,
                message + (additionalMessage.length > 0 ? "; " + String.join("; ", additionalMessage) : "")
        );
    }

    public InternalServiceException(ErrorCode enums, String... additionalMessage) {
        this(
                enums.getHttpCode(),
                enums.getCode(),
                enums.getMessage() + (additionalMessage.length > 0 ? "; " + String.join("; ", additionalMessage) : "")
        );
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
