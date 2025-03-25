package vn.com.lcx.common.database.handler.statement.output;

import java.sql.CallableStatement;
import java.sql.SQLException;

public class DoubleSqlStatementHandler implements OutputSqlStatementHandler<Double> {

    private final static DoubleSqlStatementHandler INSTANCE = new DoubleSqlStatementHandler();

    private DoubleSqlStatementHandler() {
    }

    public static DoubleSqlStatementHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public Double handle(int index, CallableStatement statement) throws SQLException {
        return statement.getDouble(index);
    }
}
