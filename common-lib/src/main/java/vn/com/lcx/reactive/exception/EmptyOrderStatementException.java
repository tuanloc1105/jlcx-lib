package vn.com.lcx.reactive.exception;

public class EmptyOrderStatementException extends RuntimeException {
    private static final long serialVersionUID = 3505355683894804519L;

    public EmptyOrderStatementException(String message) {
        super(message);
    }

    public EmptyOrderStatementException() {
        super("The order statement cannot empty");
    }
}
