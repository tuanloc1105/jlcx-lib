package vn.com.lcx.common.database.handler.statement.output;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class StringSqlStatementHandler implements OutputSqlStatementHandler<String> {

    private final static StringSqlStatementHandler INSTANCE = new StringSqlStatementHandler();

    private StringSqlStatementHandler() {
    }

    public static StringSqlStatementHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public String handle(int index, CallableStatement statement) throws SQLException {
        return statement.getString(index);
    }
}
