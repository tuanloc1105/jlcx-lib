package vn.com.lcx.vertx.base.custom;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.impl.RouterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyRouterImpl extends RouterImpl {

    private final Logger logger = LoggerFactory.getLogger("Route register");

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
        logger.info("Configuring [ROUTE]  path [{}]", path);
        return super.route(path);
    }

    @Override
    public Route get(String path) {
        logger.info("Configuring [GET]    path [{}]", path);
        return super.get(path);
    }

    @Override
    public Route post(String path) {
        logger.info("Configuring [POST]   path [{}]", path);
        return super.post(path);
    }

    @Override
    public Route put(String path) {
        logger.info("Configuring [PUT]    path [{}]", path);
        return super.put(path);
    }

    @Override
    public Route delete(String path) {
        logger.info("Configuring [DELETE] path [{}]", path);
        return super.delete(path);
    }
}
