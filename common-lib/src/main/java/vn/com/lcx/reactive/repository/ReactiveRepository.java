package vn.com.lcx.reactive.repository;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlConnection;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.reactive.helper.SqlStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Base interface for reactive repositories providing common CRUD operations.
 * All methods are asynchronous and return a {@link Future}.
 *
 * @param <T> the entity type
 */
public interface ReactiveRepository<T> {

    /**
     * Saves the given entity to the database.
     *
     * @param context    the Vert.x routing context, typically used for request-scoped data
     * @param connection the SQL connection to use for the operation
     * @param entity     the entity to save
     * @return a {@link Future} that, when completed, contains the saved entity
     */
    Future<T> save(RoutingContext context, SqlConnection connection, T entity);

    /**
     * Updates the given entity in the database.
     *
     * @param context    the Vert.x routing context, typically used for request-scoped data
     * @param connection the SQL connection to use for the operation
     * @param entity     the entity to update
     * @return a {@link Future} that, when completed, contains the number of rows updated
     */
    Future<Integer> update(RoutingContext context, SqlConnection connection, T entity);

    /**
     * Deletes the given entity from the database.
     *
     * @param context    the Vert.x routing context, typically used for request-scoped data
     * @param connection the SQL connection to use for the operation
     * @param entity     the entity to delete
     * @return a {@link Future} that, when completed, contains the number of rows deleted
     */
    Future<Integer> delete(RoutingContext context, SqlConnection connection, T entity);

    Future<Page<T>> find(RoutingContext context,
                         SqlConnection connection,
                         SqlStatement statement,
                         ArrayList<Object> parameters,
                         Pageable pageable);

    Future<List<T>> find(RoutingContext context,
                         SqlConnection connection,
                         SqlStatement statement,
                         ArrayList<Object> parameters);

}
