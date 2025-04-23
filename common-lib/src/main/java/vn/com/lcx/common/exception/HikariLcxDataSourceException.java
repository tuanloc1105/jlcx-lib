package vn.com.lcx.common.exception;

public class HikariLcxDataSourceException extends RuntimeException {
    private static final long serialVersionUID = 9086643249924837353L;

    public HikariLcxDataSourceException(String message) {
        super(message);
    }

    public HikariLcxDataSourceException(Throwable throwable) {
        super(throwable);
    }
}
