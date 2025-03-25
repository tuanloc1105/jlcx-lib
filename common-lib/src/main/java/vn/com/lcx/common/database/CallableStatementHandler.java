package vn.com.lcx.common.database;

import vn.com.lcx.common.database.handler.statement.output.BigDecimalSqlStatementHandler;
import vn.com.lcx.common.database.handler.statement.output.BooleanSqlStatementHandler;
import vn.com.lcx.common.database.handler.statement.output.DateSqlStatementHandler;
import vn.com.lcx.common.database.handler.statement.output.DoubleSqlStatementHandler;
import vn.com.lcx.common.database.handler.statement.output.FloatSqlStatementHandler;
import vn.com.lcx.common.database.handler.statement.output.IntegerSqlStatementHandler;
import vn.com.lcx.common.database.handler.statement.output.LongSqlStatementHandler;
import vn.com.lcx.common.database.handler.statement.output.StringSqlStatementHandler;

import java.sql.CallableStatement;

public interface CallableStatementHandler<T> {
    BigDecimalSqlStatementHandler bigDecimalSqlStatementHandler = BigDecimalSqlStatementHandler.getInstance();
    BooleanSqlStatementHandler booleanSqlStatementHandler = BooleanSqlStatementHandler.getInstance();
    DateSqlStatementHandler dateSqlStatementHandler = DateSqlStatementHandler.getInstance();
    DoubleSqlStatementHandler doubleSqlStatementHandler = DoubleSqlStatementHandler.getInstance();
    FloatSqlStatementHandler floatSqlStatementHandler = FloatSqlStatementHandler.getInstance();
    IntegerSqlStatementHandler integerSqlStatementHandler = IntegerSqlStatementHandler.getInstance();
    LongSqlStatementHandler longSqlStatementHandler = LongSqlStatementHandler.getInstance();
    StringSqlStatementHandler stringSqlStatementHandler = StringSqlStatementHandler.getInstance();

    T handle(CallableStatement statement);
}
