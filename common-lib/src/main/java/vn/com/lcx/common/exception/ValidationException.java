package vn.com.lcx.common.exception;

public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 571288362450652680L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable e) {
        super(e);
    }
}
