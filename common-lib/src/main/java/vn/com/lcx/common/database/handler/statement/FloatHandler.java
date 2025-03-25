package vn.com.lcx.common.database.handler.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class FloatHandler implements SqlStatementHandler {
    private FloatHandler() {
    }

    public static FloatHandler getInstance() {
        return FloatHandlerHelper.INSTANCE;
    }

    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            ((PreparedStatement) statement).setFloat(index, 0f);
        } else {
            if (!(input instanceof Float)) {
                throw new IllegalArgumentException("input is not a boolean");
            }
            ((PreparedStatement) statement).setFloat(index, (Float) input);
        }
    }

    private static class FloatHandlerHelper {
        private static final FloatHandler INSTANCE = new FloatHandler();
    }

}
