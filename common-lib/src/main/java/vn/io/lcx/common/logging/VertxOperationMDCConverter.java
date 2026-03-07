package vn.io.lcx.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import vn.io.lcx.common.constant.CommonConstant;

public class VertxOperationMDCConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
        String traceId = event.getMDCPropertyMap().get(CommonConstant.OPERATION_NAME_MDC_KEY_NAME);
        if (traceId == null || traceId.isEmpty()) {
            Context context = Vertx.currentContext();
            if (context != null && context.get(CommonConstant.OPERATION_NAME_MDC_KEY_NAME) != null) {
                return context.get(CommonConstant.OPERATION_NAME_MDC_KEY_NAME).toString();
            }
            return CommonConstant.EMPTY_STRING;
        }
        return traceId;
    }
}
