package vn.com.lcx.reactive.wrapper;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.TransactionPropagation;
import vn.com.lcx.reactive.utils.ExecutionTimeMeasureUtils;
import vn.com.lcx.vertx.base.custom.EmptyRoutingContext;

import java.util.function.Function;

@SuppressWarnings("SqlSourceToSinkFlow")
public class PoolLcxWrapper implements Pool {

    private final Pool actualPool;

    public PoolLcxWrapper(Pool actualPool) {
        this.actualPool = actualPool;
    }

    public Future<SqlConnection> getConnection() {
        return getConnection(EmptyRoutingContext.init());
    }

    public Future<SqlConnection> getConnection(RoutingContext context) {
        return ExecutionTimeMeasureUtils.execute(
                context,
                actualPool::getConnection
        );
    }

    public Query<RowSet<Row>> query(String sql) {
        return actualPool.query(sql);
    }

    public PreparedQuery<RowSet<Row>> preparedQuery(String sql) {
        return actualPool.preparedQuery(sql);
    }

    public <T> Future<@Nullable T> withTransaction(RoutingContext context, Function<SqlConnection, Future<@Nullable T>> function) {
        return ExecutionTimeMeasureUtils.execute(
                context,
                () -> actualPool.withTransaction(function)
        );
    }

    public <T> Future<@Nullable T> withTransaction(RoutingContext context, TransactionPropagation txPropagation, Function<SqlConnection, Future<@Nullable T>> function) {
        return ExecutionTimeMeasureUtils.execute(
                context,
                () -> actualPool.withTransaction(txPropagation, function)
        );
    }

    public <T> Future<@Nullable T> withConnection(RoutingContext context, Function<SqlConnection, Future<@Nullable T>> function) {
        return ExecutionTimeMeasureUtils.execute(
                context,
                () -> actualPool.withConnection(function)
        );
    }

    public <T> Future<@Nullable T> withTransaction(Function<SqlConnection, Future<@Nullable T>> function) {
        return withTransaction(EmptyRoutingContext.init(), function);
    }

    public <T> Future<@Nullable T> withTransaction(TransactionPropagation txPropagation, Function<SqlConnection, Future<@Nullable T>> function) {
        return withTransaction(EmptyRoutingContext.init(), txPropagation, function);
    }

    public <T> Future<@Nullable T> withConnection(Function<SqlConnection, Future<@Nullable T>> function) {
        return withConnection(EmptyRoutingContext.init(), function);
    }

    public PreparedQuery<RowSet<Row>> preparedQuery(String sql, PrepareOptions options) {
        return actualPool.preparedQuery(sql, options);
    }

    public Future<Void> close() {
        return actualPool.close();
    }

    public int size() {
        return actualPool.size();
    }
}
