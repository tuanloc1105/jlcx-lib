package com.example;

import vn.io.lcx.vertx.base.annotation.app.VertxApplication;
import vn.io.lcx.vertx.base.custom.MyVertxDeployment;

@VertxApplication
public class App {
    public static void main(String[] args) {
        MyVertxDeployment.getInstance().deployVerticle(App.class);
    }
}
