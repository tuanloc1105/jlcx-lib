package vn.com.lcx.common.database.handler.statement;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ClobHandler implements SqlStatementHandler {
    private ClobHandler() {
    }

    public static ClobHandler getInstance() {
        return ClobHandlerHelper.INSTANCE;
    }

    @Override
    public void handle(int index, Object input, Statement statement) throws SQLException {
        if (statement == null) {
            throw new NullPointerException("statement is null");
        }
        if (!(statement instanceof PreparedStatement)) {
            throw new IllegalArgumentException("statement is not a PreparedStatement");
        }
        if (input == null) {
            ((PreparedStatement) statement).setClob(index, new Reader() {
                @Override
                public int read(char[] cbuf, int off, int len) throws IOException {
                    return 0;
                }

                @Override
                public void close() throws IOException {

                }
            });
        } else {
            if (!(input instanceof Clob)) {
                throw new IllegalArgumentException("input is not a setBigDecimal");
            }
            ((PreparedStatement) statement).setClob(index, (Clob) input);
        }

    }

    private static class ClobHandlerHelper {
        private static final ClobHandler INSTANCE = new ClobHandler();
    }

}
