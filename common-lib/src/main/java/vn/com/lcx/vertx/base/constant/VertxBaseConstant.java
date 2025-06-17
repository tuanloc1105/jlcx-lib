package vn.com.lcx.vertx.base.constant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VertxBaseConstant {

    public static final Logger requestLogger = LoggerFactory.getLogger("request");
    public static final Logger responseLogger = LoggerFactory.getLogger("response");
    public static final Logger exceptionLogger = LoggerFactory.getLogger("exception");

    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    public static final String PROCESSED_TIME_HEADER_NAME = "Processed-Time";
    public static final String TRACE_HEADER_NAME = "Trace";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    private VertxBaseConstant() {
    }
}
