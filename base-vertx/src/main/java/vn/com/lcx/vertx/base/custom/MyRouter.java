package vn.com.lcx.vertx.base.custom;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public interface MyRouter extends Router {

    /**
     * Create a router
     *
     * @param vertx the Vert.x instance
     * @return the router
     */
    static Router router(Vertx vertx) {
        return new MyRouterImpl(vertx);
    }

}
