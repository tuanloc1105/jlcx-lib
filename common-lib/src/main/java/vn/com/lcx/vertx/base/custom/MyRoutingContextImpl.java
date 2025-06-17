package vn.com.lcx.vertx.base.custom;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.impl.RouteImpl;
import io.vertx.ext.web.impl.RouterImpl;
import io.vertx.ext.web.impl.RoutingContextImpl;

import java.util.Set;

public class MyRoutingContextImpl extends RoutingContextImpl {
    public MyRoutingContextImpl(String mountPoint, RouterImpl router, HttpServerRequest request, Set<RouteImpl> routes) {
        super(mountPoint, router, request, routes);
    }
}
