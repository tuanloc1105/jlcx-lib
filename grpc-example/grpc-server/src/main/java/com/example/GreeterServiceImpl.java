package com.example;

import examples.GreeterService;
import examples.HelloReply;
import examples.HelloRequest;
import io.vertx.core.Future;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.custom.EmptyRoutingContext;

@Component
public class GreeterServiceImpl extends GreeterService {

    @Override
    public Future<HelloReply> sayHello(HelloRequest request) {
        LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Receive a request: " + request.getName());
        return Future.succeededFuture(HelloReply.newBuilder()
                .setMessage("Hello " + request.getName())
                .build());
    }

}
