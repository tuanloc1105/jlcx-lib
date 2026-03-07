package vn.io.lcx.reactive.functional;

import io.vertx.core.Future;

import java.util.List;

public interface RowBatchCallback<T, U> {
    Future<U> handle(List<T> batch, U previousResult);
}
