package vn.com.lcx.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class LcxThreadFactory {

    static class MyThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadCount = new AtomicInteger(0);

        public MyThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, namePrefix + "-" + threadCount.incrementAndGet());
            // thread.setPriority(Thread.NORM_PRIORITY);
            // thread.setDaemon(true);
            return thread;
        }
    }

}
