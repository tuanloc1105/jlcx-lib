package vn.com.lcx.common.database.handler.statement.output;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;

public class BigDecimalSqlStatementHandler implements OutputSqlStatementHandler<BigDecimal> {

    private final static BigDecimalSqlStatementHandler INSTANCE = new BigDecimalSqlStatementHandler();

    private BigDecimalSqlStatementHandler() {
    }

    public static BigDecimalSqlStatementHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public BigDecimal handle(int index, CallableStatement statement) throws SQLException {
        return statement.getBigDecimal(index);
    }
}
