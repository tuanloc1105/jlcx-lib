package vn.com.lcx.jpa.exception;

public class JpaException extends RuntimeException {
    private static final long serialVersionUID = -8583715547243535606L;

    public JpaException(String message) {
        super(message);
    }

    public JpaException(Throwable cause) {
        super(cause);
    }
}
