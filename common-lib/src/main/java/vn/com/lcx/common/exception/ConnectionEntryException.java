package vn.com.lcx.common.exception;

public class ConnectionEntryException extends RuntimeException {
    private static final long serialVersionUID = 7592470042230028579L;

    public ConnectionEntryException(String message) {
        super(message);
    }

    public ConnectionEntryException(Throwable throwable) {
        super(throwable);
    }
}
