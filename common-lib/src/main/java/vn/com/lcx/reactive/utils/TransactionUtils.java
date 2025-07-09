package vn.com.lcx.reactive.utils;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;

import java.util.function.Function;

public final class TransactionUtils {

    private TransactionUtils() {
    }

    public static <T> Future<T> executeInTransaction(final Pool pool,
                                                     final Function<SqlConnection, Future<T>> transactionFunction) {
        return pool.getConnection()
                .compose(connection ->
                        connection.begin()
                                .compose(transaction ->
                                        transactionFunction.apply(connection)
                                                .compose(result ->
                                                        transaction.commit()
                                                                .map(v -> result)
                                                )
                                                .onFailure(err ->
                                                        transaction.rollback()
                                                                .onComplete(rollbackResult -> {
                                                                    if (rollbackResult.failed()) {
                                                                        err.addSuppressed(rollbackResult.cause());
                                                                    }
                                                                })
                                                )
                                )
                                .eventually(connection::close)
                );
    }

}
