package vn.com.lcx.reactive.exception;

public class NonUniqueQueryResult extends RuntimeException {
    private static final long serialVersionUID = 3505355683894804519L;

    public NonUniqueQueryResult(String message) {
        super(message);
    }

    public NonUniqueQueryResult() {
        super("The result of query return more than 1 row");
    }
}
