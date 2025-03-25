package vn.com.lcx.common.thread;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface BaseExecutor<RETURN_TYPE> {

    void addNewTask(Callable<RETURN_TYPE> task);

    void addNewTasks(List<Callable<RETURN_TYPE>> tasks);

    ExecutorService createExecutorService();

    void setMinThread(int nThreads);

    void setMaxThread(int nThreads);

    List<RETURN_TYPE> executeTasks();

    void cancelFutureTasks(Future<RETURN_TYPE> futureTasks);

}
