package vn.com.lcx.common.exception;

public class DuplicateInstancesException extends RuntimeException {
    private static final long serialVersionUID = 7728924895234817801L;

    public DuplicateInstancesException(String message) {
        super(message);
    }
}
