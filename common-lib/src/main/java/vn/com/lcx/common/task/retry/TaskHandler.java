package vn.com.lcx.common.task.retry;

public interface TaskHandler<I, O> {

    O doTask(I input) throws Exception;

}
