package vn.com.lcx.reactive.repository;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.common.database.pageable.Pageable;
import vn.com.lcx.common.ref.Ref;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.jpa.exception.CodeGenError;
import vn.com.lcx.reactive.context.EntityMappingContainer;
import vn.com.lcx.reactive.exception.NonUniqueQueryResult;
import vn.com.lcx.reactive.helper.SqlStatement;
import vn.com.lcx.reactive.utils.QueryProcessor;
import vn.com.lcx.reactive.wrapper.SqlConnectionLcxWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base interface for reactive repositories providing common CRUD operations.
 * All methods are asynchronous and return a {@link Future}.
 *
 * @param <T> the entity type
 */
public interface ReactiveRepository<T> {

    private static String extractDatabasePlaceholder(SqlConnection connection) {
        String databaseName = connection.databaseMetadata().productName();
        String placeholder;
        switch (databaseName) {
            case "PostgreSQL":
                placeholder = "$";
                break;
            case "Microsoft SQL Server":
                placeholder = "@p";
                break;
            case "MySQL":
            case "MariaDB":
            case "Oracle":
                placeholder = "?";
                break;
            default:
                throw new CodeGenError("Unsupported database type");
        }
        return placeholder;
    }

    private static <U> List<U> mappingResult(Class<U> outputClazz, RowSet<Row> rowSet) {
        final List<U> result = new ArrayList<>();
        for (Row row : rowSet) {
            result.add(EntityMappingContainer.<U>getMapping(outputClazz.getName()).vertxRowMapping(row));
        }
        return result;
    }

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

    /**
     * Executes a paginated SQL SELECT query and returns a {@link Page} of results.
     * <p>
     * The method:
     * <ol>
     *   <li>Executes the main query with pagination</li>
     *   <li>Maps the result rows to the target entity type</li>
     *   <li>Executes the COUNT query to determine total number of records</li>
     *   <li>Creates and returns a {@link Page} object</li>
     * </ol>
     *
     * @param context     the Vert.x routing context for logging and request metadata
     * @param connection  the SQL connection used to execute the queries
     * @param statement   the SQL builder containing SELECT and COUNT statements
     * @param parameters  the list of query parameters
     * @param pageable    page number and size information
     * @param outputClazz the class to map rows into
     * @param <U>         the output type
     * @return a {@link Future} completing with a {@link Page} of results
     */
    default <U> Future<Page<U>> find(RoutingContext context,
                                     SqlConnection connection,
                                     SqlStatement statement,
                                     ArrayList<Object> parameters,
                                     Pageable pageable,
                                     Class<U> outputClazz) {
        String placeholder = extractDatabasePlaceholder(connection);
        final Ref<Double> countStartingTime = Ref.init();
        final double startingTime = (double) System.currentTimeMillis();
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
                            final List<U> result = mappingResult(outputClazz, rowSet);
                            final double duration = ((double) System.currentTimeMillis()) - startingTime;
                            LogUtils.writeLog(context, LogUtils.Level.TRACE, "Executed SQL in {} ms", duration);
                            countStartingTime.setVal((double) System.currentTimeMillis());
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
                                    final double duration = ((double) System.currentTimeMillis()) - countStartingTime.getVal();
                                    LogUtils.writeLog(context, LogUtils.Level.TRACE, "Executed SQL in {} ms", duration);
                                    return Page.create(rs, countRs, pageable.getPageNumber(), pageable.getPageSize());
                                })
                );
    }

    /**
     * Executes a SQL SELECT query (non-paginated) and maps the results into a list
     * of strongly-typed objects.
     *
     * @param context     the Vert.x routing context for logging and request metadata
     * @param connection  the SQL connection used to execute the query
     * @param statement   the SQL builder containing the SELECT statement
     * @param parameters  the list of query parameters
     * @param outputClazz the class type to map each row into
     * @param <U>         the output type
     * @return a {@link Future} completing with a list of mapped results
     */
    default <U> Future<List<U>> find(RoutingContext context,
                                     SqlConnection connection,
                                     SqlStatement statement,
                                     ArrayList<Object> parameters,
                                     Class<U> outputClazz) {
        String placeholder = extractDatabasePlaceholder(connection);
        final double startingTime = (double) System.currentTimeMillis();
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
                            final List<U> result = mappingResult(outputClazz, rowSet);
                            final double duration = ((double) System.currentTimeMillis()) - startingTime;
                            LogUtils.writeLog(context, LogUtils.Level.TRACE, "Executed SQL in {} ms", duration);
                            return result;
                        }
                );
    }

    /**
     * Executes a SQL SELECT query expected to return exactly one row.
     * <p>
     * Behavior:
     * <ul>
     *   <li>Returns {@link Optional#empty()} if no rows are found</li>
     *   <li>Returns {@link Optional#of(Object)} if exactly one row is found</li>
     *   <li>Throws {@link NonUniqueQueryResult} if more than one row is returned</li>
     * </ul>
     *
     * @param context     the Vert.x routing context for logging and request metadata
     * @param connection  the SQL connection used to execute the query
     * @param statement   the SQL builder containing the SELECT statement
     * @param parameters  the list of query parameters
     * @param outputClazz the class type to map the row into
     * @param <U>         the output type
     * @return a {@link Future} completing with an {@link Optional} result
     */
    default <U> Future<Optional<U>> findOne(RoutingContext context,
                                            SqlConnection connection,
                                            SqlStatement statement,
                                            ArrayList<Object> parameters,
                                            Class<U> outputClazz) {
        String placeholder = extractDatabasePlaceholder(connection);
        final double startingTime = (double) System.currentTimeMillis();
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
                            if (rowSet.size() == 0) {
                                final double duration = ((double) System.currentTimeMillis()) - startingTime;
                                LogUtils.writeLog(context, LogUtils.Level.TRACE, "Executed SQL in {} ms", duration);
                                return Optional.empty();
                            }
                            if (rowSet.size() > 1) {
                                final double duration = ((double) System.currentTimeMillis()) - startingTime;
                                LogUtils.writeLog(context, LogUtils.Level.TRACE, "Executed SQL in {} ms", duration);
                                throw new NonUniqueQueryResult();
                            }
                            final List<U> result = mappingResult(outputClazz, rowSet);
                            final double duration = ((double) System.currentTimeMillis()) - startingTime;
                            LogUtils.writeLog(context, LogUtils.Level.TRACE, "Executed SQL in {} ms", duration);
                            return Optional.of(result.get(0));
                        }
                );
    }

    /**
     * Executes a SQL SELECT query and returns only the first row (if any).
     * <p>
     * Unlike {@link #findOne}, this method does not enforce uniqueness and simply
     * returns the first element of the result set.
     *
     * @param context     the Vert.x routing context for logging and request metadata
     * @param connection  the SQL connection used to execute the query
     * @param statement   the SQL builder containing the SELECT statement
     * @param parameters  the list of query parameters
     * @param outputClazz the class type to map the row into
     * @param <U>         the output type
     * @return a {@link Future} completing with an {@link Optional} of the first row
     */
    default <U> Future<Optional<U>> findFirst(RoutingContext context,
                                              SqlConnection connection,
                                              SqlStatement statement,
                                              ArrayList<Object> parameters,
                                              Class<U> outputClazz) {
        String placeholder = extractDatabasePlaceholder(connection);
        final double startingTime = (double) System.currentTimeMillis();
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
                            if (rowSet.size() == 0) {
                                final double duration = ((double) System.currentTimeMillis()) - startingTime;
                                LogUtils.writeLog(context, LogUtils.Level.TRACE, "Executed SQL in {} ms", duration);
                                return Optional.empty();
                            }
                            final List<U> result = mappingResult(outputClazz, rowSet);
                            final double duration = ((double) System.currentTimeMillis()) - startingTime;
                            LogUtils.writeLog(context, LogUtils.Level.TRACE, "Executed SQL in {} ms", duration);
                            return Optional.of(result.get(0));
                        }
                );
    }

}
