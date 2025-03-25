package vn.com.lcx.common.database.handler.statement.output;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class LongSqlStatementHandler implements OutputSqlStatementHandler<Long> {

    private final static LongSqlStatementHandler INSTANCE = new LongSqlStatementHandler();

    private LongSqlStatementHandler() {
    }

    public static LongSqlStatementHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public Long handle(int index, CallableStatement statement) throws SQLException {
        return statement.getLong(index);
    }
}
