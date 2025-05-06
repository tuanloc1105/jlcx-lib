package com.example.lcx.config;

import io.vertx.ext.web.RoutingContext;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.annotation.app.ContextHandler;
import vn.com.lcx.vertx.base.custom.VertxContextHandler;

@ContextHandler(order = 0)
@Component
public class AuthHandler implements VertxContextHandler {
    @Override
    public void handle(RoutingContext ctx) {
        LogUtils.writeLog(LogUtils.Level.INFO, "hello");
        ctx.next();
    }
}
