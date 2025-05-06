package com.example.lcx.config;

import com.example.lcx.object.dto.UserJWTTokenInfo;
import com.google.gson.Gson;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.app.ContextHandler;
import vn.com.lcx.vertx.base.context.AuthContext;
import vn.com.lcx.vertx.base.custom.VertxContextHandler;

@ContextHandler(order = 0)
@Component
@RequiredArgsConstructor
public class AuthHandler implements VertxContextHandler {

    private final Gson gson;

    @Override
    public void handle(RoutingContext routingContext) {
        if (routingContext.request().path().contains("login")) {
            routingContext.next();
        } else {
            try {
                final JsonObject jsonObject = routingContext.user().get("accessToken");
                final var userInfo = gson.fromJson(jsonObject.encode(), UserJWTTokenInfo.class);
                AuthContext.set(userInfo);
                routingContext.next();
                AuthContext.clear();
            } catch (Exception e) {
                routingContext.end(e.getMessage());
            }
        }
    }
}
