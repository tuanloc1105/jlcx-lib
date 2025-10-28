package vn.com.lcx.reactive.utils;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import vn.com.lcx.common.utils.LogUtils;

import java.util.function.Supplier;

public final class ReactiveRetryUtils {

    private ReactiveRetryUtils() {
    }

    public static <T> Future<T> execute(Vertx vertx,
                                        RoutingContext context,
                                        Supplier<Future<T>> function) {
        return execute(vertx, context, function, 2, 1000);
    }

    public static <T> Future<T> execute(Vertx vertx,
                                        RoutingContext context,
                                        Supplier<Future<T>> function,
                                        int retryTimes,
                                        long delayMs) {
        Promise<T> promise = Promise.promise();

        executeWithRetry(vertx, context, function, retryTimes, delayMs, promise);

        return promise.future();
    }

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
