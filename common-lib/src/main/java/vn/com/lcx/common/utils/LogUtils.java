package vn.com.lcx.common.utils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import vn.com.lcx.common.constant.CommonConstant;

import java.util.Optional;


@SuppressWarnings("DuplicatedCode")
public final class LogUtils {

    private LogUtils() {
    }

    public static void writeLog(Level level, String message, Object... messageParameter) {
        val fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        val classNameArray = fullClassName.split("\\.");
        val simpleClassName = classNameArray[classNameArray.length - 1];
        val methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        val lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        val stepName = buildStepNameLogMessage(fullClassName, methodName, simpleClassName, lineNumber);
        val logToWrite = String.format(
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
        val fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        val classNameArray = fullClassName.split("\\.");
        val simpleClassName = classNameArray[classNameArray.length - 1];
        val methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        val lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        val stepName = buildStepNameLogMessage(fullClassName, methodName, simpleClassName, lineNumber);
        val logToWrite = String.format(
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
        val fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        val logToWrite = String.format(
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
        val fullClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        val logToWrite = String.format(
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
        val methodNamePart = StringUtils.isNotBlank(methodName) ? "[%-" + 40 + "s]" : "[%s]";
        val stepNamePart = StringUtils.isNotBlank(stepName) ? "[%-" + 50 + "s]" : "[%s]";
        return methodNamePart + " " +
                stepNamePart + " " +
                ">>>>>>>> ";
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
