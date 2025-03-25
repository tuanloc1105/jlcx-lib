package vn.com.lcx.common.database.handler.statement.output;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class FloatSqlStatementHandler implements OutputSqlStatementHandler<Float> {

    private final static FloatSqlStatementHandler INSTANCE = new FloatSqlStatementHandler();

    private FloatSqlStatementHandler() {
    }

    public static FloatSqlStatementHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public Float handle(int index, CallableStatement statement) throws SQLException {
        return statement.getFloat(index);
    }
}
