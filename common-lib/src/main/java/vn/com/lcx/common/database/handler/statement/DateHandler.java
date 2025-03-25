package vn.com.lcx.common.database.handler.statement;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DateHandler implements SqlStatementHandler {
    private DateHandler() {
    }

    public static DateHandler getInstance() {
        return DateHandlerHelper.INSTANCE;
    }

    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            //((PreparedStatement) statement).setDate(index, new Date(System.currentTimeMillis()));
            ((PreparedStatement) statement).setDate(index, null);
        } else {
            if (!(input instanceof Date)) {
                throw new IllegalArgumentException("input is not a boolean");
            }
            ((PreparedStatement) statement).setDate(index, (Date) input);
        }
    }

    private static class DateHandlerHelper {
        private static final DateHandler INSTANCE = new DateHandler();
    }

}
