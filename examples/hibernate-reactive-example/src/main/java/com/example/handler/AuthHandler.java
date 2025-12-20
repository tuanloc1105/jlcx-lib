package com.example.handler;

import com.example.model.dto.UserJWTTokenInfo;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.vertx.base.annotation.app.ContextHandler;
import vn.com.lcx.vertx.base.custom.VertxContextHandler;

import static vn.com.lcx.common.constant.CommonConstant.OPERATION_NAME_MDC_KEY_NAME;

@ContextHandler(order = 0)
@Component
@RequiredArgsConstructor
public class AuthHandler implements VertxContextHandler {

    private final Gson gson;

    @Override
    public void handle(RoutingContext ctx) {
        if (ctx.request().path().contains("login") || ctx.request().path().contains("create_new")) {
            ctx.next();
        } else {
            try {
                final JsonObject jsonObject = ctx.user().get("accessToken");
                final var userInfo = gson.fromJson(jsonObject.encode(), UserJWTTokenInfo.class);
                ctx.put(OPERATION_NAME_MDC_KEY_NAME, userInfo.getUsername());
                ctx.put(CommonConstant.CURRENT_USER, userInfo);
                ctx.next();
            } catch (Exception e) {
                ctx.response().setStatusCode(401).end(e.getMessage());
            }
        }

    }
}
