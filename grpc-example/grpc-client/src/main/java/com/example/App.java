package com.example;

import com.example.grpc.GreeterGrpcClient;
import com.example.grpc.HelloReply;
import com.example.grpc.HelloRequest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.PostConstruct;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.annotation.app.VertxApplication;
import vn.com.lcx.vertx.base.custom.EmptyRoutingContext;
import vn.com.lcx.vertx.base.custom.MyVertxDeployment;

@VertxApplication
@Component
@RequiredArgsConstructor
public class App {

    private final Vertx vertx;

    public static void main(String[] args) {
        MyVertxDeployment.getInstance().deployVerticle(App.class);
    }

    @PostConstruct
    public void post() {
        GrpcClient client = GrpcClient.client(vertx);
        GreeterGrpcClient greeterClient = GreeterGrpcClient.create(
                client,
                SocketAddress.inetSocketAddress(7070, "localhost")
        );
        Future<HelloReply> response = greeterClient.sayHello(HelloRequest.newBuilder().setName("John").build());
        response.onSuccess(result ->
                {
                    LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Service responded: " + response.result().getMessage());
                    System.exit(0);
                }
        );
        response.onFailure(err ->
                {
                    LogUtils.writeLog(EmptyRoutingContext.init(), LogUtils.Level.INFO, "Service failure: " + response.cause().getMessage());
                    System.exit(0);
                }
        );
    }

}
