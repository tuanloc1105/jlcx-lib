package vn.com.lcx.reactive.exception;

public class EmptySelectStatementException extends RuntimeException {
    private static final long serialVersionUID = 3505355683894804519L;

    public EmptySelectStatementException(String message) {
        super(message);
    }

    public EmptySelectStatementException() {
        super("The select statement cannot empty");
    }
}
