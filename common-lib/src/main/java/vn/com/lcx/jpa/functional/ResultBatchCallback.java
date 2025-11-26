package vn.com.lcx.jpa.functional;

import java.util.List;

@FunctionalInterface
public interface ResultBatchCallback<T> {
    void handle(List<T> batch);
}
