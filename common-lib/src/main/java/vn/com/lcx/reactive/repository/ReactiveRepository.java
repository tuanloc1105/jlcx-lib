package vn.com.lcx.reactive.repository;

import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;

public interface ReactiveRepository<T> {

    Future<RowSet<Row>> save(SqlConnection client, T entity);

    Future<RowSet<Row>> update(SqlConnection client, T entity);

    Future<RowSet<Row>> delete(SqlConnection client, T entity);

}
