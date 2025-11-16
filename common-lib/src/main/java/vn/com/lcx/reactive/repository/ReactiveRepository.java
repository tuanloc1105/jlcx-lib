package vn.com.lcx.reactive.repository;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.jpa.exception.CodeGenError;
import vn.com.lcx.reactive.context.EntityMappingContainer;
import vn.com.lcx.reactive.helper.SqlStatement;
import vn.com.lcx.reactive.utils.QueryProcessor;
import vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper;

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

    default <U> Future<Page<U>> find(RoutingContext context,
                                     SqlConnection connection,
                                     SqlStatement statement,
                                     ArrayList<Object> parameters,
                                     Pageable pageable,
                                     Class<U> outputClazz) {
        String databaseName = connection.databaseMetadata().productName();
        String placeholder;
        if (databaseName.equals("PostgreSQL")) {
            placeholder = "$";
        } else if (databaseName.equals("MySQL") || databaseName.equals("MariaDB")) {
            placeholder = "?";
        } else if (databaseName.equals("Microsoft SQL Server")) {
            placeholder = "@p";
        } else if (databaseName.equals("Oracle")) {
            placeholder = "?";
        } else {
            throw new CodeGenError("Unsupported database type");
        }
        // noinspection SqlSourceToSinkFlow
        return SqlConnectionLcxWrapper.init(connection, context)
                .preparedQuery(
                        QueryProcessor.processQueryStatement(
                                statement.finalizeQueryStatement(pageable),
                                parameters,
                                placeholder
                        )
                )
                .execute(Tuple.tuple(parameters))
                .map(rowSet ->
                        {
                            final List<U> result = new ArrayList<>();
                            for (Row row : rowSet) {
                                result.add(EntityMappingContainer.<U>getMapping(outputClazz.getName()).vertxRowMapping(row));
                            }
                            return result;
                        }
                )
                .compose(rs ->
                        SqlConnectionLcxWrapper.init(connection, context)
                                .preparedQuery(
                                        QueryProcessor.processQueryStatement(
                                                statement.finalizeCountStatement(),
                                                parameters,
                                                placeholder
                                        )
                                )
                                .execute(Tuple.tuple(parameters))
                                .map(rowSet -> {
                                    long countRs = 0L;
                                    for (Row row : rowSet) {
                                        countRs = countRs + row.getLong(0);
                                        break;
                                    }
                                    return Page.create(rs, countRs, pageable.getPageNumber(), pageable.getPageSize());
                                })
                );
    }

    default <U> Future<List<U>> find(RoutingContext context,
                                     SqlConnection connection,
                                     SqlStatement statement,
                                     ArrayList<Object> parameters,
                                     Class<U> outputClazz) {
        String databaseName = connection.databaseMetadata().productName();
        String placeholder;
        if (databaseName.equals("PostgreSQL")) {
            placeholder = "$";
        } else if (databaseName.equals("MySQL") || databaseName.equals("MariaDB")) {
            placeholder = "?";
        } else if (databaseName.equals("Microsoft SQL Server")) {
            placeholder = "@p";
        } else if (databaseName.equals("Oracle")) {
            placeholder = "?";
        } else {
            throw new CodeGenError("Unsupported database type");
        }
        // noinspection SqlSourceToSinkFlow
        return SqlConnectionLcxWrapper.init(connection, context)
                .preparedQuery(
                        QueryProcessor.processQueryStatement(
                                statement.finalizeQueryStatement(),
                                parameters,
                                placeholder
                        )
                )
                .execute(Tuple.tuple(parameters))
                .map(rowSet ->
                        {
                            final List<U> result = new ArrayList<>();
                            for (Row row : rowSet) {
                                result.add(EntityMappingContainer.<U>getMapping(outputClazz.getName()).vertxRowMapping(row));
                            }
                            return result;
                        }
                );
    }

}
