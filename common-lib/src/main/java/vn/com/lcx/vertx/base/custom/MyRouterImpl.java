package vn.com.lcx.vertx.base.custom;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.impl.RouterImpl;
import vn.com.lcx.common.utils.LogUtils;

public class MyRouterImpl extends RouterImpl {
    public MyRouterImpl(Vertx vertx) {
        super(vertx);
    }

    @Override
    public void handle(HttpServerRequest request) {
        // LogUtils.writeLog(EmptyRoutingContext.init(),
        //         LogUtils.Level.INFO,
        //         "Router: {} accepting request {} {}",
        //         System.identityHashCode(this),
        //         request.method(),
        //         request.absoluteURI()
        //
        // );
        super.handle(request);
    }

    @Override
    public synchronized Route route(String path) {
        LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Configuring route for path [{}]", path);
        return super.route(path);
    }

    @Override
    public Route get(String path) {
        LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Configuring get path [{}]", path);
        return super.get(path);
    }

    @Override
    public Route post(String path) {
        LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Configuring post path [{}]", path);
        return super.post(path);
    }

    @Override
    public Route put(String path) {
        LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Configuring put path [{}]", path);
        return super.put(path);
    }

    @Override
    public Route delete(String path) {
        LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Configuring delete path [{}]", path);
        return super.delete(path);
    }
}
