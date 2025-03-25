package vn.com.lcx.common.database.handler.statement.output;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DateSqlStatementHandler implements OutputSqlStatementHandler<LocalDateTime> {

    private final static DateSqlStatementHandler INSTANCE = new DateSqlStatementHandler();

    private DateSqlStatementHandler() {
    }

    public static DateSqlStatementHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public LocalDateTime handle(int index, CallableStatement statement) throws SQLException {
        Timestamp time = statement.getTimestamp(index);
        return time != null ? time.toLocalDateTime() : null;
    }
}
