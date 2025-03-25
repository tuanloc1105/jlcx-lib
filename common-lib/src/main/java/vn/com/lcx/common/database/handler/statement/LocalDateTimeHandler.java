package vn.com.lcx.common.database.handler.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class LocalDateTimeHandler implements SqlStatementHandler {
    private LocalDateTimeHandler() {
    }

    public static LocalDateTimeHandler getInstance() {
        return LocalDateTimeHandlerHelper.INSTANCE;
    }

    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            ((PreparedStatement) statement).setTimestamp(index, null);
        } else {
            if (!(input instanceof LocalDateTime)) {
                throw new IllegalArgumentException("input is not a boolean");
            }
            ((PreparedStatement) statement).setTimestamp(index, Timestamp.valueOf((LocalDateTime) input));
        }
    }

    private static class LocalDateTimeHandlerHelper {
        private static final LocalDateTimeHandler INSTANCE = new LocalDateTimeHandler();
    }

}
