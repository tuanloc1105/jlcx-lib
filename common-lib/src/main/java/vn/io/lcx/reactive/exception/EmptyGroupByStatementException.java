package vn.io.lcx.reactive.exception;

public class EmptyGroupByStatementException extends RuntimeException {
    private static final long serialVersionUID = 7612348957234891023L;

    public EmptyGroupByStatementException(String message) {
        super(message);
    }

    public EmptyGroupByStatementException() {
        super("The group by statement cannot empty");
    }
}
