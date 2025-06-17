package vn.com.lcx.reactive.repository;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;

public interface ReactiveRepository<T> {

    Future<RowSet<Row>> save(RoutingContext context, SqlConnection client, T entity);

    Future<RowSet<Row>> update(RoutingContext context, SqlConnection client, T entity);

    Future<RowSet<Row>> delete(RoutingContext context, SqlConnection client, T entity);

}
