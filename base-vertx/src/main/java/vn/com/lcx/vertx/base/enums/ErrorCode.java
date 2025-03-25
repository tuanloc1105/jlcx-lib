package vn.com.lcx.vertx.base.enums;

public interface ErrorCode {
    int getHttpCode();

    int getCode();

    String getMessage();
}
