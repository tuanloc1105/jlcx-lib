package vn.com.lcx.reactive.repository;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

public interface ReactiveRepository<T> {

    Future<Integer> save(SqlConnection client, T entity);

    Future<Integer> update(SqlConnection client, T entity);

    Future<Integer> delete(SqlConnection client, T entity);

}
