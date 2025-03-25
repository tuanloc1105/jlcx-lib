package vn.com.lcx.common.exception;

public class LCXDataSourceException extends RuntimeException {
    private static final long serialVersionUID = 7592470042230028579L;

    public LCXDataSourceException(String message) {
        super(message);
    }

    public LCXDataSourceException(Throwable throwable) {
        super(throwable);
    }
}
