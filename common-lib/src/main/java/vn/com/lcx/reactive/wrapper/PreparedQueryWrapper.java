package vn.com.lcx.reactive.wrapper;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

@RequiredArgsConstructor
public class PreparedQueryWrapper<T> implements PreparedQuery<T> {

    private final PreparedQuery<T> realPreparedQuery;
    private final RoutingContext context;

    @Override
    public Future<T> execute(Tuple tuple) {
        return realPreparedQuery.execute(tuple);
    }

    @Override
    public Future<T> executeBatch(List<Tuple> batch) {
        return realPreparedQuery.executeBatch(batch);
    }

    @Override
    public Future<T> execute() {
        return realPreparedQuery.execute();
    }

    @Override
    public <R> PreparedQuery<SqlResult<R>> collecting(Collector<Row, ?, R> collector) {
        return realPreparedQuery.collecting(collector);
    }

    @Override
    public <U> PreparedQuery<RowSet<U>> mapping(Function<Row, U> mapper) {
        return realPreparedQuery.mapping(mapper);
    }
}
