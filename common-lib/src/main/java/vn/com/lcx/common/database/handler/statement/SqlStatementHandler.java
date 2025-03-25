package vn.com.lcx.common.database.handler.statement;

import java.sql.SQLException;
import java.sql.Statement;

public interface SqlStatementHandler {
    void handle(int index, Object input, Statement statement) throws SQLException;
}
