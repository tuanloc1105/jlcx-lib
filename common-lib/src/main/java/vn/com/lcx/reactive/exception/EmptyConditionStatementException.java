package vn.com.lcx.reactive.exception;

public class EmptyConditionStatementException extends RuntimeException {
    private static final long serialVersionUID = 3505355683894804519L;

    public EmptyConditionStatementException(String message) {
        super(message);
    }

    public EmptyConditionStatementException() {
        super("The condition statement cannot empty");
    }
}
