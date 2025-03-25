package vn.com.lcx.common.database.handler.statement.output;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class BooleanSqlStatementHandler implements OutputSqlStatementHandler<Boolean> {

    private final static BooleanSqlStatementHandler INSTANCE = new BooleanSqlStatementHandler();

    private BooleanSqlStatementHandler() {
    }

    public static BooleanSqlStatementHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public Boolean handle(int index, CallableStatement statement) throws SQLException {
        return statement.getBoolean(index);
    }
}
