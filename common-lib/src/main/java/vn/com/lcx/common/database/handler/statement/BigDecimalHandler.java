package vn.com.lcx.common.database.handler.statement;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class BigDecimalHandler implements SqlStatementHandler {
    private BigDecimalHandler() {
    }

    public static BigDecimalHandler getInstance() {
        return BigDecimalHandlerHelper.INSTANCE;
    }

    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            // ((PreparedStatement) statement).setBigDecimal(index, BigDecimal.ZERO);
            ((PreparedStatement) statement).setBigDecimal(index, null);
        } else {
            if (!(input instanceof BigDecimal)) {
                throw new IllegalArgumentException("input is not a setBigDecimal");
            }
            ((PreparedStatement) statement).setBigDecimal(index, (BigDecimal) input);
        }
    }

    private static class BigDecimalHandlerHelper {
        private static final BigDecimalHandler INSTANCE = new BigDecimalHandler();
    }

}
