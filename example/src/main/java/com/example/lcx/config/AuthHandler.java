package com.example.lcx.config;

import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.constant.CommonConstant;
import vn.com.lcx.common.context.AuthContext;
import vn.com.lcx.vertx.base.annotation.app.ContextHandler;
import vn.com.lcx.vertx.base.custom.VertxContextHandler;

import static vn.com.lcx.common.constant.CommonConstant.OPERATION_NAME_MDC_KEY_NAME;

@ContextHandler(order = 0)
@Component
@RequiredArgsConstructor
public class AuthHandler implements VertxContextHandler {

    private final Gson gson;

    @Override
    public void handle(RoutingContext routingContext) {
        if (routingContext.request().path().contains("login") ||
                routingContext.request().path().contains("create_new")) {
            routingContext.next();
        } else {
            try {
                final JsonObject jsonObject = routingContext.user().get("accessToken");
                final var userInfo = gson.fromJson(jsonObject.encode(), UserJWTTokenInfo.class);
                routingContext.put(OPERATION_NAME_MDC_KEY_NAME, userInfo.getUsername());
                routingContext.put(CommonConstant.CURRENT_USER, userInfo);
                AuthContext.set(userInfo);
                routingContext.next();
                AuthContext.clear();
            } catch (Exception e) {
                routingContext.end(e.getMessage());
            }
        }
    }
}
