package vn.com.lcx.reactive.wrapper;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import vn.com.lcx.common.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public class PreparedQueryWrapper<T> implements PreparedQuery<T> {

    private final PreparedQuery<T> realPreparedQuery;
    private final RoutingContext context;

    public PreparedQueryWrapper(PreparedQuery<T> realPreparedQuery, RoutingContext context) {
        this.realPreparedQuery = realPreparedQuery;
        this.context = context;
    }

    @Override
    public Future<T> execute(Tuple tuple) {
        return realPreparedQuery.execute(extractTupleValue(tuple));
    }

    @Override
    public Future<T> executeBatch(List<Tuple> batch) {
        List<Tuple> actualBatch = new ArrayList<>();
        batch.forEach(tuple -> actualBatch.add(extractTupleValue(tuple)));
        return realPreparedQuery.executeBatch(actualBatch);
    }

    private Tuple extractTupleValue(Tuple tuple) {
        StringBuilder parametersLog = new StringBuilder("parameters:");
        final int size = tuple.size();
        final var actualTuple = Tuple.tuple();
        int count = 1;
        for (int i = 0; i < size; ++i) {
            Object value = tuple.getValue(i);
            if (value instanceof List) {
                final var listVal = (List<?>) value;
                for (Object o : listVal) {
                    parametersLog.append(
                            String.format(
                                    "\n\t- parameter %s: %s",
                                    String.format("%-3d %-20s)", count++, "(" + o.getClass().getSimpleName()),
                                    o
                            )
                    );
                    actualTuple.addValue(o);
                }
            } else {
                parametersLog.append(
                        String.format(
                                "\n\t- parameter %s: %s",
                                String.format("%-3d %-20s)", count++, "(" + value.getClass().getSimpleName()),
                                value
                        )
                );
                actualTuple.addValue(value);
            }
        }
        LogUtils.writeLog(context, LogUtils.Level.INFO, parametersLog.toString());
        return actualTuple;
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
