package vn.io.lcx.reactive.utils;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import vn.io.lcx.common.utils.LogUtils;
import vn.io.lcx.vertx.base.custom.EmptyRoutingContext;

import java.util.function.Supplier;

public final class ExecutionTimeMeasureUtils {

    private ExecutionTimeMeasureUtils() {
    }

    public static <T> Future<T> execute(RoutingContext context, Supplier<Future<T>> function) {
        final var startTime = (double) System.currentTimeMillis();
        return  function.get().onComplete(it ->
                {
                    final var endingTime = (double) System.currentTimeMillis();
                    final var duration = endingTime - startTime;
                    LogUtils.writeLog(ExecutionTimeMeasureUtils.class, context, LogUtils.Level.DEBUG, "Executed in {} ms", duration);
                }
        );
    }

    public static <T> Future<T> execute(Supplier<Future<T>> function) {
        return execute(EmptyRoutingContext.init(), function);
    }

}
