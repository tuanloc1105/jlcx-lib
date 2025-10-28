package vn.com.lcx.reactive.utils;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import vn.com.lcx.common.utils.LogUtils;

import java.util.function.Supplier;

/**
 * Utility class providing retry mechanisms for executing asynchronous operations in Vert.x.
 * <p>
 * This class allows developers to execute an asynchronous operation that returns a {@link Future},
 * and automatically retry the operation upon failure, with a configurable number of retries and delay between attempts.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * ReactiveRetryUtils.execute(vertx, context, () -> asyncOperation(), 3, 2000)
 *     .onSuccess(result -> {
 *         // Handle success
 *     })
 *     .onFailure(err -> {
 *         // Handle failure after all retries
 *     });
 * }</pre>
 *
 * <p>
 * By default, logging is performed through {@link LogUtils} at the following levels:
 * <ul>
 *   <li>{@code WARN} - For each retry attempt</li>
 *   <li>{@code ERROR} - When all retry attempts have failed</li>
 * </ul>
 * </p>
 *
 * <p>This class is final and cannot be instantiated.</p>
 */
public final class ReactiveRetryUtils {

    private ReactiveRetryUtils() {
    }

    /**
     * Executes the given asynchronous {@link Supplier} and retries it once (total of two attempts)
     * if it fails, with a default delay of 1000 milliseconds between retries.
     *
     * @param vertx    the Vert.x instance used to schedule delayed retry attempts
     * @param context  the {@link RoutingContext} used for contextual logging
     * @param function the supplier that produces a {@link Future} representing the asynchronous operation
     * @param <T>      the type of the operation result
     * @return a {@link Future} that completes successfully with the result of {@code function.get()}
     * or fails after all retry attempts are exhausted
     */
    public static <T> Future<T> execute(Vertx vertx,
                                        RoutingContext context,
                                        Supplier<Future<T>> function) {
        return execute(vertx, context, function, 2, 1000);
    }

    /**
     * Executes the given asynchronous {@link Supplier} and retries it on failure for a specified number of times,
     * with a fixed delay between each retry.
     *
     * @param vertx      the Vert.x instance used to schedule delayed retry attempts
     * @param context    the {@link RoutingContext} used for contextual logging
     * @param function   the supplier that produces a {@link Future} representing the asynchronous operation
     * @param retryTimes total number of attempts to execute the operation (must be ≥ 1)
     * @param delayMs    delay in milliseconds between retry attempts
     * @param <T>        the type of the operation result
     * @return a {@link Future} that completes successfully with the result of {@code function.get()},
     * or fails after all retry attempts are exhausted
     */
    public static <T> Future<T> execute(Vertx vertx,
                                        RoutingContext context,
                                        Supplier<Future<T>> function,
                                        int retryTimes,
                                        long delayMs) {
        Promise<T> promise = Promise.promise();

        executeWithRetry(vertx, context, function, retryTimes, delayMs, promise);

        return promise.future();
    }

    /**
     * Internal recursive method that performs the retry logic.
     * <p>
     * If the operation fails and there are retries left, it schedules a new attempt after the specified delay.
     * When all retries are exhausted, the promise is failed with the last encountered exception.
     * </p>
     *
     * @param vertx       the Vert.x instance used to schedule delayed retry attempts
     * @param context     the {@link RoutingContext} used for contextual logging
     * @param function    the supplier that produces a {@link Future} representing the asynchronous operation
     * @param retriesLeft number of remaining retry attempts
     * @param delayMs     delay in milliseconds before the next retry
     * @param promise     the {@link Promise} to complete or fail with the final result
     * @param <T>         the type of the operation result
     */
    private static <T> void executeWithRetry(Vertx vertx,
                                             RoutingContext context,
                                             Supplier<Future<T>> function,
                                             int retriesLeft,
                                             long delayMs,
                                             Promise<T> promise) {

        function.get()
                .onSuccess(promise::complete)
                .onFailure(e ->
                        {
                            if (retriesLeft > 1) {
                                LogUtils.writeLog(context,
                                        "Retrying... attempts left: " + (retriesLeft - 1),
                                        e, LogUtils.Level.WARN);

                                vertx.setTimer(delayMs, id ->
                                        executeWithRetry(vertx, context, function, retriesLeft - 1, delayMs, promise));
                            } else {
                                String msg = "All retry attempts failed.";
                                LogUtils.writeLog(context, msg, e, LogUtils.Level.ERROR);
                                promise.fail(e != null ? e : new RuntimeException(msg));
                            }
                        }
                );
    }

}
