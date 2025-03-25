package vn.com.lcx.common.database.handler.statement.output;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class IntegerSqlStatementHandler implements OutputSqlStatementHandler<Integer> {

    private final static IntegerSqlStatementHandler INSTANCE = new IntegerSqlStatementHandler();

    private IntegerSqlStatementHandler() {
    }

    public static IntegerSqlStatementHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public Integer handle(int index, CallableStatement statement) throws SQLException {
        return statement.getInt(index);
    }
}
