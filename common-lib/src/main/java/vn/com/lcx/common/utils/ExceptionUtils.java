package vn.com.lcx.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static String getStackTrace(Throwable throwable) {
        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            return String.format("Can not get stack trace: %s | error: %s", e.getMessage(), throwable.getMessage());
        }
        // StringWriter stringWriter = new StringWriter();
        // PrintWriter printWriter = new PrintWriter(stringWriter);
        // throwable.printStackTrace(printWriter);
        // return stringWriter.toString();
    }

}
