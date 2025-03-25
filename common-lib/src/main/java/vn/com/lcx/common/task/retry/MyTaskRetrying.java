package vn.com.lcx.common.task.retry;

import lombok.var;
import vn.com.lcx.common.utils.LogUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class MyTaskRetrying<I, O> {

    public O doTaskAndRetrying(I input,
                               final int timesToRetrying,
                               final long timeWaitingToRetry,
                               final ChronoUnit unit,
                               TaskHandler<I, O> taskHandler) {
        Throwable exception = null;
        O output = null;
        try {
            output = taskHandler.doTask(input);
        } catch (Throwable e) {
            LogUtils.writeLog("An exception has been occurred, retrying", e);
            exception = e;
        }
        var timeRetried = 0;
        while (exception != null && timeRetried < timesToRetrying) {
            try {
                Thread.sleep(Duration.of(timeWaitingToRetry, unit).toMillis());
                output = taskHandler.doTask(input);
                exception = null;
            } catch (Throwable e) {
                LogUtils.writeLog("An exception has been occurred, retrying", e);
                exception = e;
            }
            timeRetried++;
        }
        return output;
    }

}
