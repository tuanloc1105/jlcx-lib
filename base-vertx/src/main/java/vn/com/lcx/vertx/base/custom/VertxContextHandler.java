package vn.com.lcx.vertx.base.custom;

import io.vertx.ext.web.RoutingContext;

public interface VertxContextHandler {

    void handle(RoutingContext ctx);

}
