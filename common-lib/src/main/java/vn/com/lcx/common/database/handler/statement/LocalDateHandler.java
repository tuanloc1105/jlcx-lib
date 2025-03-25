package vn.com.lcx.common.database.handler.statement;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class LocalDateHandler implements SqlStatementHandler {
    private LocalDateHandler() {
    }

    public static LocalDateHandler getInstance() {
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
            if (!(input instanceof LocalDate)) {
                throw new IllegalArgumentException("input is not a boolean");
            }
            ((PreparedStatement) statement).setDate(index, Date.valueOf((LocalDate) input));
        }
    }

    private static class LocalDateTimeHandlerHelper {
        private static final LocalDateHandler INSTANCE = new LocalDateHandler();
    }

}
