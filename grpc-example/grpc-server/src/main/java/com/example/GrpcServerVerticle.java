package com.example;

import examples.GreeterGrpcService;
import examples.GreeterService;
import io.vertx.core.http.HttpServer;
import io.vertx.grpc.server.GrpcServer;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Verticle;
import vn.com.lcx.vertx.base.verticle.VertxBaseVerticle;

@Verticle
@RequiredArgsConstructor
public class GrpcServerVerticle extends VertxBaseVerticle {

    private final GreeterService greeterService;

    @Override
    public io.vertx.core.Future<io.vertx.core.http.HttpServer> start() {
        GrpcServer grpcServer = GrpcServer.server(vertx);
        final var service = GreeterGrpcService.of(greeterService);
        grpcServer.addService(service);
        HttpServer server = vertx.createHttpServer();
        return server.requestHandler(grpcServer).listen(7070);
    }

}
