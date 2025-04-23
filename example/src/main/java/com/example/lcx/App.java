package com.example.lcx;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.annotation.app.ComponentScan;
import vn.com.lcx.vertx.base.annotation.app.VertxApplication;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.custom.MyVertxDeployment;

@VertxApplication
@ComponentScan(value = {"com.example"})
@Controller
public class App {
    public static void main(String[] args) {
        MyVertxDeployment.getInstance().deployVerticle(App.class);

        Vertx vertx = ClassPool.getInstance("vertx", Vertx.class);
        JsonObject request = new JsonObject()
                .put("action", "sayHello")
                .put("name", "App");

        vertx.eventBus().request("process.json", request, reply -> {
            if (reply.succeeded()) {
                JsonObject response = (JsonObject) reply.result().body();
                LogUtils.writeLog(LogUtils.Level.INFO, "Verticle A received reply: {}", response.encodePrettily());
            } else {
                LogUtils.writeLog("Failed to get reply", reply.cause());
            }
        });
    }
}
