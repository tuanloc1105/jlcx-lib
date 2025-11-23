package vn.com.lcx.reactive.exception;

public class EmptyFromStatementException extends RuntimeException {
    private static final long serialVersionUID = 3505355683894804519L;

    public EmptyFromStatementException(String message) {
        super(message);
    }

    public EmptyFromStatementException() {
        super("The from statement cannot empty");
    }
}
