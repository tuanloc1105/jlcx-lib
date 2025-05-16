package vn.com.lcx.vertx.base.verticle;

import io.vertx.core.VerticleBase;
import io.vertx.ext.web.RoutingContext;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.common.utils.UUIDv7;

import static vn.com.lcx.common.constant.CommonConstant.TRACE_ID_MDC_KEY_NAME;

public class VertxBaseVerticle extends VerticleBase {

    public void createUUIDHandler(RoutingContext context) {
        LogUtils.writeLog(LogUtils.Level.DEBUG, context.toString());
        LogUtils.writeLog(LogUtils.Level.DEBUG, context.getClass().getName());
        val traceFromInput = (String) context.get(TRACE_ID_MDC_KEY_NAME);
        if (StringUtils.isBlank(traceFromInput)) {
            val trace = UUIDv7.randomUUID().toString().replace(CommonConstant.HYPHEN, CommonConstant.EMPTY_STRING);
            context.put(TRACE_ID_MDC_KEY_NAME, trace.replace(CommonConstant.HYPHEN, CommonConstant.EMPTY_STRING));
            LogUtils.writeLog(LogUtils.Level.INFO, "Create UUID Handler: {}", trace);
        }
        context.next();
    }

}
