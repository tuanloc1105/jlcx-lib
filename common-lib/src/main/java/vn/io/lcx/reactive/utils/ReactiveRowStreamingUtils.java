package vn.io.lcx.reactive.utils;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import vn.io.lcx.reactive.functional.RowBatchCallback;
import vn.io.lcx.reactive.helper.SqlStatement;

import java.util.ArrayList;
import java.util.List;

import static vn.io.lcx.reactive.utils.ReactiveConnectionUtils.extractDatabasePlaceholder;
import static vn.io.lcx.reactive.utils.ReactiveConnectionUtils.mappingResult;

/**
 * Utility class for streaming database rows in a reactive manner using Vert.x SQL client.
 * <p>
 * This class provides methods to stream query results, allowing for efficient processing
 * of large datasets by fetching rows in batches and processing them asynchronously.
 * </p>
 */
public final class ReactiveRowStreamingUtils {

    private ReactiveRowStreamingUtils() {
    }

    /**
     * Streams rows from the database based on the provided SQL statement and parameters.
     * <p>
     * This method executes the query and fetches rows in batches. For each batch, the rows are mapped
     * to the specified output class and passed to the provided callback for processing.
     * The processing is stateful, allowing results to be passed from one batch to the next.
     * </p>
     *
     * @param context     the routing context (used for initializing connection wrapper)
     * @param pool        the database connection pool
     * @param statement   the SQL statement to execute
     * @param parameters  the list of parameters for the SQL statement
     * @param batchSize   the number of rows to fetch per batch
     * @param outputClazz the class to map the rows to
     * @param callback    the callback to handle each batch of results, returning a Future
     * @param <T>         the type of the output class
     * @param <U>         the type of the result passed between batches
     */
    public static <T, U> void stream(final RoutingContext context,
                                     final Pool pool,
                                     final SqlStatement statement,
                                     final ArrayList<Object> parameters,
                                     final int batchSize,
                                     final Class<T> outputClazz,
                                     final RowBatchCallback<T, U> callback) {
        // noinspection SqlSourceToSinkFlow
        pool.withConnection(connection ->
                vn.io.lcx.reactive.wrapper.SqlConnectionLcxWrapper.init(connection, context)
                        .prepare(
                                QueryProcessor.processQueryStatement(
                                        statement.finalizeQueryStatement(),
                                        parameters,
                                        extractDatabasePlaceholder(connection)
                                )
                        )
                        .compose(pq -> {
                            // Create a cursor
                            Cursor cursor = pq.cursor(Tuple.tuple(parameters));
                            return startStreaming(cursor, batchSize, outputClazz, callback, null);
                        })
        );
    }

    /**
     * Helper method to recursively fetch and process rows in batches.
     *
     * @param cursor         the database cursor
     * @param batchSize      the batch size
     * @param outputClazz    the output class type
     * @param callback       the callback for processing
     * @param previousResult the result from the previous batch processing, or null for the first batch
     * @param <T>            the type of the row mapped object
     * @param <U>            the type of the accumulating result
     * @return a Future completing when the streaming is finished
     */
    private static <T, U> Future<U> startStreaming(final Cursor cursor,
                                                   final int batchSize,
                                                   final Class<T> outputClazz,
                                                   final RowBatchCallback<T, U> callback,
                                                   final U previousResult) {
        return cursor
                .read(batchSize)
                .compose(rows -> {
                    final List<T> result = mappingResult(outputClazz, rows);
                    return callback.handle(result, previousResult)
                            .compose(handlingResult -> {
                                // Check for more ?
                                if (cursor.hasMore()) {
                                    // Repeat the process...
                                    return startStreaming(cursor, batchSize, outputClazz, callback, handlingResult);
                                } else {
                                    // No more rows - close the cursor
                                    return cursor.close().map(handlingResult);
                                }
                            });
                });
    }

}
