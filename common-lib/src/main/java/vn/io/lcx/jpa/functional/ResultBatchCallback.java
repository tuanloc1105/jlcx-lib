package vn.io.lcx.jpa.functional;

import java.util.List;

@FunctionalInterface
public interface ResultBatchCallback<T> {
    void handle(List<T> batch);
}
