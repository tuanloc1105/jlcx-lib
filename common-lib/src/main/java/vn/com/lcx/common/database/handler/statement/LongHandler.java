package vn.com.lcx.common.database.handler.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class LongHandler implements SqlStatementHandler {
    private LongHandler() {
    }

    public static LongHandler getInstance() {
        return LongHandlerHelper.INSTANCE;
    }

    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            ((PreparedStatement) statement).setLong(index, 0);
        } else {
            if (!(input instanceof Long)) {
                throw new IllegalArgumentException("input is not a boolean");
            }
            ((PreparedStatement) statement).setLong(index, (Long) input);
        }
    }

    private static class LongHandlerHelper {
        private static final LongHandler INSTANCE = new LongHandler();
    }

}
