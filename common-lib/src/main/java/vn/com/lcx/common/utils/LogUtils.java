package vn.com.lcx.common.utils;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import vn.com.lcx.common.constant.CommonConstant;

import java.util.ArrayList;

@SuppressWarnings("DuplicatedCode")
public final class LogUtils {

    private LogUtils() {
    }

    public static void writeLog(Class<?> clazz, Level level, String message, Object... messageParameter) {
        switch (level) {
            case INFO:
                LoggerFactory.getLogger(clazz).info(message, messageParameter);
                break;
            case WARN:
                LoggerFactory.getLogger(clazz).warn(message, messageParameter);
                break;
            case ERROR:
                LoggerFactory.getLogger(clazz).error(message, messageParameter);
                break;
            case DEBUG:
                LoggerFactory.getLogger(clazz).debug(message, messageParameter);
                break;
            case TRACE:
                LoggerFactory.getLogger(clazz).trace(message, messageParameter);
                break;
        }
    }

    public static void writeLog(Class<?> clazz, String message, Throwable throwable, Level... level) {
        if (level.length == 0) {
            LoggerFactory.getLogger(clazz).error(message, throwable);
        } else {
            switch (level[0]) {
                case INFO:
                    throw new IllegalArgumentException("Exception logging do not accept level INFO");
                case WARN:
                    LoggerFactory.getLogger(clazz).warn(message, throwable);
                    break;
                case ERROR:
                    LoggerFactory.getLogger(clazz).error(message, throwable);
                    break;
                case DEBUG:
                    LoggerFactory.getLogger(clazz).debug(message, throwable);
                case TRACE:
                    LoggerFactory.getLogger(clazz).trace(message, throwable);
            }

        }
    }

    public static void writeLog(String name, Level level, String message, Object... messageParameter) {
        switch (level) {
            case INFO:
                LoggerFactory.getLogger(name).info(message, messageParameter);
                break;
            case WARN:
                LoggerFactory.getLogger(name).warn(message, messageParameter);
                break;
            case ERROR:
                LoggerFactory.getLogger(name).error(message, messageParameter);
                break;
            case DEBUG:
                LoggerFactory.getLogger(name).debug(message, messageParameter);
                break;
            case TRACE:
                LoggerFactory.getLogger(name).trace(message, messageParameter);
                break;
        }
    }

    public static void writeLog(String name, String message, Throwable throwable, Level... level) {
        if (level.length == 0) {
            LoggerFactory.getLogger(name).error(message, throwable);
        } else {
            switch (level[0]) {
                case INFO:
                    throw new IllegalArgumentException("Exception logging do not accept level INFO");
                case WARN:
                    LoggerFactory.getLogger(name).warn(message, throwable);
                    break;
                case ERROR:
                    LoggerFactory.getLogger(name).error(message, throwable);
                    break;
                case DEBUG:
                    LoggerFactory.getLogger(name).debug(message, throwable);
                case TRACE:
                    LoggerFactory.getLogger(name).trace(message, throwable);
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

    public static void writeLog(Class<?> clazz, RoutingContext context, Level level, String message, Object... messageParameter) {
        final var logKeyList = new ArrayList<String>();
        final var vertxCurrentContext = Vertx.currentContext();
        try {
            context.data().forEach((key, value) ->
                    {
                        if (CommonConstant.TRACE_ID_MDC_KEY_NAME.equals(key)) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                        }
                        if (CommonConstant.OPERATION_NAME_MDC_KEY_NAME.equals(key)) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                        }
                        if (String.valueOf(key).startsWith("log-key")) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(key, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(key, value + CommonConstant.EMPTY_STRING);
                            logKeyList.add(key);
                        }
                    }
            );
            writeLog(clazz, level, message, messageParameter);
        } finally {
            MDC.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            if (vertxCurrentContext != null) {
                vertxCurrentContext.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            }
            MDC.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            if (vertxCurrentContext != null) {
                vertxCurrentContext.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            }
            if (!logKeyList.isEmpty()) {
                for (String key : logKeyList) {
                    MDC.remove(key);
                    if (vertxCurrentContext != null) {
                        vertxCurrentContext.remove(key);
                    }
                }
            }
        }
    }

    public static void writeLog(Class<?> clazz, RoutingContext context, String message, Throwable throwable, Level... level) {
        final var logKeyList = new ArrayList<String>();
        final var vertxCurrentContext = Vertx.currentContext();
        try {
            context.data().forEach((key, value) ->
                    {
                        if (CommonConstant.TRACE_ID_MDC_KEY_NAME.equals(key)) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                        }
                        if (CommonConstant.OPERATION_NAME_MDC_KEY_NAME.equals(key)) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                        }
                        if (String.valueOf(key).startsWith("log-key")) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(key, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(key, value + CommonConstant.EMPTY_STRING);
                            logKeyList.add(key);
                        }
                    }
            );
            writeLog(clazz, message, throwable, level);
        } finally {
            MDC.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            if (vertxCurrentContext != null) {
                vertxCurrentContext.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            }
            MDC.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            if (vertxCurrentContext != null) {
                vertxCurrentContext.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            }
            if (!logKeyList.isEmpty()) {
                for (String key : logKeyList) {
                    MDC.remove(key);
                    if (vertxCurrentContext != null) {
                        vertxCurrentContext.remove(key);
                    }
                }
            }
        }
    }

    public static void writeLog(String name, RoutingContext context, Level level, String message, Object... messageParameter) {
        final var logKeyList = new ArrayList<String>();
        final var vertxCurrentContext = Vertx.currentContext();
        try {
            context.data().forEach((key, value) ->
                    {
                        if (CommonConstant.TRACE_ID_MDC_KEY_NAME.equals(key)) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                        }
                        if (CommonConstant.OPERATION_NAME_MDC_KEY_NAME.equals(key)) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                        }
                        if (String.valueOf(key).startsWith("log-key")) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(key, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(key, value + CommonConstant.EMPTY_STRING);
                            logKeyList.add(key);
                        }
                    }
            );
            writeLog(name, level, message, messageParameter);
        } finally {
            MDC.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            if (vertxCurrentContext != null) {
                vertxCurrentContext.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            }
            MDC.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            if (vertxCurrentContext != null) {
                vertxCurrentContext.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            }
            if (!logKeyList.isEmpty()) {
                for (String key : logKeyList) {
                    MDC.remove(key);
                    if (vertxCurrentContext != null) {
                        vertxCurrentContext.remove(key);
                    }
                }
            }
        }
    }

    public static void writeLog(String name, RoutingContext context, String message, Throwable throwable, Level... level) {
        final var logKeyList = new ArrayList<String>();
        final var vertxCurrentContext = Vertx.currentContext();
        try {
            context.data().forEach((key, value) ->
                    {
                        if (CommonConstant.TRACE_ID_MDC_KEY_NAME.equals(key)) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(CommonConstant.TRACE_ID_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                        }
                        if (CommonConstant.OPERATION_NAME_MDC_KEY_NAME.equals(key)) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, value + CommonConstant.EMPTY_STRING);
                        }
                        if (String.valueOf(key).startsWith("log-key")) {
                            if (vertxCurrentContext != null) {
                                vertxCurrentContext.put(key, value + CommonConstant.EMPTY_STRING);
                            }
                            MDC.put(key, value + CommonConstant.EMPTY_STRING);
                            logKeyList.add(key);
                        }
                    }
            );
            writeLog(name, message, throwable, level);
        } finally {
            MDC.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            if (vertxCurrentContext != null) {
                vertxCurrentContext.remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
            }
            MDC.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            if (vertxCurrentContext != null) {
                vertxCurrentContext.remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
            }
            if (!logKeyList.isEmpty()) {
                for (String key : logKeyList) {
                    MDC.remove(key);
                    if (vertxCurrentContext != null) {
                        vertxCurrentContext.remove(key);
                    }
                }
            }
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

    public static void initContextInfo(RoutingContext context) {
        Vertx.currentContext().put(CommonConstant.TRACE_ID_MDC_KEY_NAME, context.get(CommonConstant.TRACE_ID_MDC_KEY_NAME) + CommonConstant.EMPTY_STRING);
        Vertx.currentContext().put(CommonConstant.OPERATION_NAME_MDC_KEY_NAME, context.get(CommonConstant.OPERATION_NAME_MDC_KEY_NAME) + CommonConstant.EMPTY_STRING);
    }

    public static void removeContextInfo() {
        Vertx.currentContext().remove(CommonConstant.TRACE_ID_MDC_KEY_NAME);
        Vertx.currentContext().remove(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
    }

    public enum Level {
        INFO,
        WARN,
        ERROR,
        DEBUG,
        TRACE,
    }

}
