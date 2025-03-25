package vn.com.lcx.common.database.handler.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class IntegerHandler implements SqlStatementHandler {
    private IntegerHandler() {
    }

    public static IntegerHandler getInstance() {
        return IntegerHandlerHelper.INSTANCE;
    }

    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            ((PreparedStatement) statement).setInt(index, 0);
        } else {
            if (!(input instanceof Integer)) {
                throw new IllegalArgumentException("input is not a boolean");
            }
            ((PreparedStatement) statement).setInt(index, (Integer) input);
        }
    }

    private static class IntegerHandlerHelper {
        private static final IntegerHandler INSTANCE = new IntegerHandler();
    }

}
