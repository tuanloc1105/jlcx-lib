package vn.com.lcx.jpa.exception;

public class CodeGenError extends RuntimeException {
    private static final long serialVersionUID = -2341193267580465913L;

    public CodeGenError(String message) {
        super(message);
    }
}
