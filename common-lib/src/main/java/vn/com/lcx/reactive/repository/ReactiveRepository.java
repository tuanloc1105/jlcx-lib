package vn.com.lcx.reactive.repository;

import io.vertx.sqlclient.SqlConnection;

public interface ReactiveRepository<T> {

    int save(SqlConnection client, T entity);

    int update(SqlConnection client, T entity);

    int delete(SqlConnection client, T entity);

}
