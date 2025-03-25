package vn.com.lcx.common.database.handler.statement.output;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface OutputSqlStatementHandler<T> {
    T handle(int index, CallableStatement statement) throws SQLException;
}
