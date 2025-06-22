package vn.com.lcx.reactive.wrapper;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import vn.com.lcx.common.utils.LogUtils;

@SuppressWarnings("SqlSourceToSinkFlow")
public class SqlConnectionLcxWrapper implements SqlConnection {

    private final SqlConnection realConnection;
    private final RoutingContext context;

    public SqlConnectionLcxWrapper(SqlConnection realConnection, RoutingContext context) {
        this.realConnection = realConnection;
        this.context = context;
    }

    public static SqlConnectionLcxWrapper init(SqlConnection realConnection, RoutingContext context) {
        return new SqlConnectionLcxWrapper(realConnection, context);
    }

    @Override
    public Future<PreparedStatement> prepare(String sql) {
        return realConnection.prepare(sql);
    }

    @Override
    public Future<PreparedStatement> prepare(String sql, PrepareOptions options) {
        return realConnection.prepare(sql, options);
    }

    @Override
    public SqlConnection exceptionHandler(Handler<Throwable> handler) {
        return realConnection.exceptionHandler(handler);
    }

    @Override
    public SqlConnection closeHandler(Handler<Void> handler) {
        return realConnection.closeHandler(handler);
    }

    @Override
    public Future<Transaction> begin() {
        return realConnection.begin();
    }

    @Override
    public Transaction transaction() {
        return realConnection.transaction();
    }

    @Override
    public boolean isSSL() {
        return realConnection.isSSL();
    }

    @Override
    public DatabaseMetadata databaseMetadata() {
        return realConnection.databaseMetadata();
    }

    @Override
    public Query<RowSet<Row>> query(String sql) {
        return realConnection.query(sql);
    }

    @Override
    public PreparedQuery<RowSet<Row>> preparedQuery(String sql) {
        LogUtils.writeLog(context, LogUtils.Level.INFO, sql);
        PreparedQuery<RowSet<Row>> preparedQuery = realConnection.preparedQuery(sql);
        @SuppressWarnings("UnnecessaryLocalVariable")
        PreparedQueryWrapper<RowSet<Row>> wrapper = new PreparedQueryWrapper<>(preparedQuery, context);
        return wrapper;
    }

    @Override
    public PreparedQuery<RowSet<Row>> preparedQuery(String sql, PrepareOptions options) {
        LogUtils.writeLog(context, LogUtils.Level.INFO, sql);
        PreparedQuery<RowSet<Row>> preparedQuery = realConnection.preparedQuery(sql, options);
        @SuppressWarnings("UnnecessaryLocalVariable")
        PreparedQueryWrapper<RowSet<Row>> wrapper = new PreparedQueryWrapper<>(preparedQuery, context);
        return wrapper;
    }

    @Override
    public Future<Void> close() {
        return realConnection.close();
    }
}
