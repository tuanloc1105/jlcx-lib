package vn.com.lcx.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import vn.com.lcx.common.constant.CommonConstant;

public class VertxTraceIdMDCConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
        String traceId = event.getMDCPropertyMap().get(CommonConstant.TRACE_ID_MDC_KEY_NAME);
        if (traceId == null || traceId.isEmpty()) {
            Context context = Vertx.currentContext();
            if (context != null && context.get(CommonConstant.TRACE_ID_MDC_KEY_NAME) != null) {
                return context.get(CommonConstant.TRACE_ID_MDC_KEY_NAME).toString();
            }
            return CommonConstant.EMPTY_STRING;
        }
        return traceId;
    }
}
