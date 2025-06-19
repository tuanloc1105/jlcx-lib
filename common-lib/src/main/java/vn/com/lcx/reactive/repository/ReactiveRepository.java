package vn.com.lcx.reactive.repository;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlConnection;

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
     * @param context the Vert.x routing context, typically used for request-scoped data
     * @param client the SQL connection to use for the operation
     * @param entity the entity to save
     * @return a {@link Future} that, when completed, contains the saved entity
     */
    Future<T> save(RoutingContext context, SqlConnection client, T entity);

    /**
     * Updates the given entity in the database.
     *
     * @param context the Vert.x routing context, typically used for request-scoped data
     * @param client the SQL connection to use for the operation
     * @param entity the entity to update
     * @return a {@link Future} that, when completed, contains the number of rows updated
     */
    Future<Integer> update(RoutingContext context, SqlConnection client, T entity);

    /**
     * Deletes the given entity from the database.
     *
     * @param context the Vert.x routing context, typically used for request-scoped data
     * @param client the SQL connection to use for the operation
     * @param entity the entity to delete
     * @return a {@link Future} that, when completed, contains the number of rows deleted
     */
    Future<Integer> delete(RoutingContext context, SqlConnection client, T entity);

}
