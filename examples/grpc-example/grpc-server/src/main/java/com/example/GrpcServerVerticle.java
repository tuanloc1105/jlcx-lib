package com.example;

import com.example.grpc.GreeterGrpcService;
import com.example.grpc.GreeterService;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.grpc.server.GrpcServer;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.common.annotation.Verticle;
import vn.com.lcx.vertx.base.verticle.VertxBaseVerticle;

@Verticle
@RequiredArgsConstructor
@Component
public class GrpcServerVerticle extends VertxBaseVerticle {

    private final GreeterService greeterService;

    @Override
    public Future<HttpServer> start() {
        GrpcServer grpcServer = GrpcServer.server(vertx);
        final var service = GreeterGrpcService.of(greeterService);
        grpcServer.addService(service);
        HttpServer server = vertx.createHttpServer();
        return server.requestHandler(grpcServer).listen(7070);
    }

}
