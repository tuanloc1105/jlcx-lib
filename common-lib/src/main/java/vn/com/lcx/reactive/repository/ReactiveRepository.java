package vn.com.lcx.reactive.repository;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlConnection;

public interface ReactiveRepository<T> {

    Future<T> save(RoutingContext context, SqlConnection client, T entity);

    Future<Void> update(RoutingContext context, SqlConnection client, T entity);

    Future<Void> delete(RoutingContext context, SqlConnection client, T entity);

}
