package vn.com.lcx.common.database.handler.statement.output;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetSqlStatementHandler implements OutputSqlStatementHandler<ResultSet> {

    private final static ResultSetSqlStatementHandler INSTANCE = new ResultSetSqlStatementHandler();

    private ResultSetSqlStatementHandler() {
    }

    public static ResultSetSqlStatementHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public ResultSet handle(int index, CallableStatement statement) throws SQLException {
        return (ResultSet) statement.getObject(index);
    }
}
