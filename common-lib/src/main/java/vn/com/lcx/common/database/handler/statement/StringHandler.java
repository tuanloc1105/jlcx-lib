package vn.com.lcx.common.database.handler.statement;

import vn.com.lcx.common.constant.CommonConstant;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class StringHandler implements SqlStatementHandler {
    private StringHandler() {
    }

    public static StringHandler getInstance() {
        return StringHandlerHelper.INSTANCE;
    }

    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            ((PreparedStatement) statement).setString(index, CommonConstant.EMPTY_STRING);
        } else {
            if (!(input instanceof String)) {
                throw new IllegalArgumentException("input is not a boolean");
            }
            ((PreparedStatement) statement).setString(index, (String) input);
        }
    }

    private static class StringHandlerHelper {
        private static final StringHandler INSTANCE = new StringHandler();
    }

}
