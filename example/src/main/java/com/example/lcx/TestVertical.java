package com.example.lcx;

import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import vn.com.lcx.common.annotation.Verticle;
import vn.com.lcx.common.utils.LogUtils;
import vn.com.lcx.vertx.base.verticle.VertxBaseVerticle;

@Verticle
public class TestVertical extends VertxBaseVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.eventBus().consumer("process.json", message -> {
            JsonObject request = (JsonObject) message.body();
            LogUtils.writeLog(LogUtils.Level.INFO,"Verticle B received: {}", request.encode());

            String name = request.getString("name");
            JsonObject response = new JsonObject()
                    .put("message", "Hello " + name)
                    .put("status", "success");

            message.reply(response);
        });
    }

}
