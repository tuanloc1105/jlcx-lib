package vn.com.lcx.common.mail;

public class MailPropertiesEmptyError extends RuntimeException {
    private static final long serialVersionUID = -6126070335623970739L;

    public MailPropertiesEmptyError(String message) {
        super(message);
    }
}
