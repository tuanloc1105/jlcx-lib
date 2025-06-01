package vn.com.lcx.jpa.exception;

public class IllegalEntityClassException extends RuntimeException {
    private static final long serialVersionUID = 8522724646072872845L;

    public IllegalEntityClassException(String message) {
        super(message);
    }

}
