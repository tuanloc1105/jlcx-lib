package vn.com.lcx.common.database.handler.statement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DoubleHandler implements SqlStatementHandler {
    private DoubleHandler() {
    }

    public static DoubleHandler getInstance() {
        return DoubleHandlerHelper.INSTANCE;
    }

    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            ((PreparedStatement) statement).setDouble(index, 0d);
        } else {
            if (!(input instanceof Double)) {
                throw new IllegalArgumentException("input is not a boolean");
            }
            ((PreparedStatement) statement).setDouble(index, (Double) input);
        }
    }

    private static class DoubleHandlerHelper {
        private static final DoubleHandler INSTANCE = new DoubleHandler();
    }

}
