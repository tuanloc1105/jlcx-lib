package vn.com.lcx.jpa.functional;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface BatchCallback {
    void handle(List<Map<String, String>> batch);
}
