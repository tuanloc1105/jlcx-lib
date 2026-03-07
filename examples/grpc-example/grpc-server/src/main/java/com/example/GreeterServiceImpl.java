package com.example;

import com.example.grpc.GreeterService;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.vertx.core.Future;
import vn.io.lcx.common.annotation.Component;
import vn.io.lcx.common.utils.LogUtils;
import vn.io.lcx.vertx.base.custom.EmptyRoutingContext;

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
