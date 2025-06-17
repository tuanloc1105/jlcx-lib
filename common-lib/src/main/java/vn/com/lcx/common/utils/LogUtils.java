package vn.com.lcx.common.utils;

import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import vn.com.lcx.common.constant.CommonConstant;

@SuppressWarnings("DuplicatedCode")
public final class LogUtils {

    private LogUtils() {
    }

    public static void writeLog(Level level, String message, Object... messageParameter) {
        final var fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        final var classNameArray = fullClassName.split("\\.");
        final var simpleClassName = classNameArray[classNameArray.length - 1];
        final var methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        final var lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        final var stepName = buildStepNameLogMessage(fullClassName, methodName, simpleClassName, lineNumber);
        final var logToWrite = String.format(
                buildLogTemplate(methodName, stepName),
                MyStringUtils.getLastChars(methodName, 40),
                MyStringUtils.getLastChars(stepName, 50)
        ) + System.lineSeparator() + message;
        switch (level) {
            case INFO:
                LoggerFactory.getLogger(fullClassName).info(logToWrite, messageParameter);
                break;
            case WARN:
                LoggerFactory.getLogger(fullClassName).warn(logToWrite, messageParameter);
                break;
            case ERROR:
                LoggerFactory.getLogger(fullClassName).error(logToWrite, messageParameter);
                break;
            case DEBUG:
                LoggerFactory.getLogger(fullClassName).debug(logToWrite, messageParameter);
                break;
            case TRACE:
                LoggerFactory.getLogger(fullClassName).trace(logToWrite, messageParameter);
                break;
        }
    }

    public static void writeLog(String message, Throwable throwable, Level... level) {
        final var fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        final var classNameArray = fullClassName.split("\\.");
        final var simpleClassName = classNameArray[classNameArray.length - 1];
        final var methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        final var lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        final var stepName = buildStepNameLogMessage(fullClassName, methodName, simpleClassName, lineNumber);
        final var logToWrite = String.format(
                buildLogTemplate(methodName, stepName),
                MyStringUtils.getLastChars(methodName, 40),
                MyStringUtils.getLastChars(stepName, 50)
        ) + System.lineSeparator() + message;
        if (level.length == 0) {
            LoggerFactory.getLogger(fullClassName).error(logToWrite, throwable);
        } else {
            switch (level[0]) {
                case INFO:
                    throw new IllegalArgumentException("Exception logging do not accept level INFO");
                case WARN:
                    LoggerFactory.getLogger(fullClassName).warn(logToWrite, throwable);
                    break;
                case ERROR:
                    LoggerFactory.getLogger(fullClassName).error(logToWrite, throwable);
                    break;
                case DEBUG:
                    throw new IllegalArgumentException("Exception logging do not accept level DEBUG");
                case TRACE:
                    throw new IllegalArgumentException("Exception logging do not accept level TRACE");
            }

        }
    }

    public static void writeLog2(Level level, String message, Object... messageParameter) {
        final var fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        final var logToWrite = String.format(
                buildLogTemplate(CommonConstant.EMPTY_STRING, CommonConstant.EMPTY_STRING),
                MyStringUtils.getLastChars(CommonConstant.EMPTY_STRING, 40),
                MyStringUtils.getLastChars(CommonConstant.EMPTY_STRING, 50)
        ) + (StringUtils.isBlank(message) || message.startsWith("\n") ? message : System.lineSeparator() + System.lineSeparator() + message);

        switch (level) {
            case INFO:
                LoggerFactory.getLogger(fullClassName).info(logToWrite, messageParameter);
                break;
            case WARN:
                LoggerFactory.getLogger(fullClassName).warn(logToWrite, messageParameter);
                break;
            case ERROR:
                LoggerFactory.getLogger(fullClassName).error(logToWrite, messageParameter);
                break;
            case DEBUG:
                LoggerFactory.getLogger(fullClassName).debug(logToWrite, messageParameter);
                break;
            case TRACE:
                LoggerFactory.getLogger(fullClassName).trace(logToWrite, messageParameter);
                break;
        }
    }

    public static void writeLog2(String message, Throwable throwable, Level... level) {
        final var fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        final var logToWrite = String.format(
                buildLogTemplate(CommonConstant.EMPTY_STRING, CommonConstant.EMPTY_STRING),
                MyStringUtils.getLastChars(CommonConstant.EMPTY_STRING, 40),
                MyStringUtils.getLastChars(CommonConstant.EMPTY_STRING, 50)
        ) + System.lineSeparator() + message;
        if (level.length == 0) {
            LoggerFactory.getLogger(fullClassName).error(logToWrite, throwable);
        } else {
            switch (level[0]) {
                case INFO:
                    throw new IllegalArgumentException("Exception logging do not accept level INFO");
                case WARN:
                    LoggerFactory.getLogger(fullClassName).warn(logToWrite, throwable);
                    break;
                case ERROR:
                    LoggerFactory.getLogger(fullClassName).error(logToWrite, throwable);
                    break;
                case DEBUG:
                    throw new IllegalArgumentException("Exception logging do not accept level DEBUG");
                case TRACE:
                    throw new IllegalArgumentException("Exception logging do not accept level TRACE");
            }

        }
    }

    private static String buildLogTemplate(final String methodName,
                                           final String stepName) {
        final var methodNamePart = StringUtils.isNotBlank(methodName) ? "[%-" + 40 + "s]" : "[%s]";
        final var stepNamePart = StringUtils.isNotBlank(stepName) ? "[%-" + 50 + "s]" : "[%s]";
        return methodNamePart + " " +
                stepNamePart + " " +
                ">>>>>>>> ";
    }

    public static void writeLog(RoutingContext context, Level level, String message, Object... messageParameter) {
        try {
            final var trace = context.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            final var operation = context.<String>get(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            MDC.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, trace);
            MDC.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, operation);
            writeLog(level, message, messageParameter);
        } finally {
            MDC.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            MDC.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
        }
    }

    public static void writeLog(RoutingContext context, String message, Throwable throwable, Level... level) {
        try {
            final var trace = context.<String>get(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            final var operation = context.<String>get(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            MDC.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, trace);
            MDC.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, operation);
            writeLog(message, throwable, level);
        } finally {
            MDC.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            MDC.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
        }
    }

    private static String buildStepNameLogMessage(String fullClassName, String methodName, String simpleClassName, int lineNumber) {
        return String.format(
                "at %s.%s(%s.java:%d)",
                fullClassName,
                methodName,
                simpleClassName,
                lineNumber
        );
    }

    public enum Level {
        INFO,
        WARN,
        ERROR,
        DEBUG,
        TRACE,
    }

}
