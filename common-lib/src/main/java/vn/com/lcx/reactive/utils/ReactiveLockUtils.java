package vn.com.lcx.reactive.utils;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.function.Supplier;

public final class ReactiveLockUtils {

    private ReactiveLockUtils() {
    }

    public static <T> Future<T> executeInTransaction(Vertx vertx, String lockName, Supplier<Future<T>> function) {
        final var sharedData = vertx.sharedData();
        return sharedData.getLock(lockName).compose(
                lock ->
                        function.get()
                                .onComplete(ar -> lock.release())
        );
    }

    public static <T> Future<T> executeInTransactionWithTimeout(Vertx vertx, String lockName, long timeoutMs, Supplier<Future<T>> function) {
        final var sharedData = vertx.sharedData();
        return sharedData.getLockWithTimeout(lockName, timeoutMs).compose(
                lock ->
                        function.get()
                                .onComplete(ar -> lock.release())
        );
    }

    public static <T> Future<T> executeInTransactionWithTimeout(Vertx vertx, String lockName, Supplier<Future<T>> function) {
        return executeInTransactionWithTimeout(vertx, lockName, 1L, function);
    }

}
