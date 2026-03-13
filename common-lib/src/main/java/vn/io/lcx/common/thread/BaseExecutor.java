package vn.io.lcx.common.thread;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public interface BaseExecutor<RETURN_TYPE> extends AutoCloseable {

    void addNewTask(Callable<RETURN_TYPE> task);

    void addNewTasks(List<Callable<RETURN_TYPE>> tasks);

    void executeTasksWithCountDownLatch();

    void cancelFutureTasks(Future<RETURN_TYPE> futureTasks);

    CompletableFuture<Void> runAsync(Runnable runnable);

    CompletableFuture<Void> runAsync(Runnable... runnable);

    @Override
    void close();

}
