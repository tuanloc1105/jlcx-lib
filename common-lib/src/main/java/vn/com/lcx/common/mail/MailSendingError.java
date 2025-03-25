package vn.com.lcx.common.mail;

public class MailSendingError extends RuntimeException {
    private static final long serialVersionUID = 6445361697459667265L;

    public MailSendingError(String message) {
        super(message);
    }

    public MailSendingError(String message, Throwable cause) {
        super(message, cause);
    }

    public MailSendingError(Throwable cause) {
        super(cause);
    }
}
