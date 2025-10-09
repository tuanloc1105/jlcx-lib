package vn.com.lcx.common.mail;

import io.vertx.core.Future;
import io.vertx.core.WorkerExecutor;

import java.util.Map;

public class ReactiveMailSender {

    private final WorkerExecutor workerExecutor;

    public ReactiveMailSender(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    public Future<Map<String, String>> sendEmail(final MailProperties mailProperties) {
        return workerExecutor.executeBlocking(
                () -> MailHelper.sendHTMLEmail(mailProperties),
                false
        );
    }

}
