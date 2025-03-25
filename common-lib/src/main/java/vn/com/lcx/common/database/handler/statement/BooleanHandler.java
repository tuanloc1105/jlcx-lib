package vn.com.lcx.common.database.handler.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class BooleanHandler implements SqlStatementHandler {
    private BooleanHandler() {
    }

    public static BooleanHandler getInstance() {
        return BooleanHandlerHelper.INSTANCE;
    }

    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            ((PreparedStatement) statement).setBoolean(index, false);
        } else {
            if (!(input instanceof Boolean)) {
                throw new IllegalArgumentException("input is not a boolean");
            }
            ((PreparedStatement) statement).setBoolean(index, (Boolean) input);
        }
    }

    private static class BooleanHandlerHelper {
        private static final BooleanHandler INSTANCE = new BooleanHandler();
    }

}
