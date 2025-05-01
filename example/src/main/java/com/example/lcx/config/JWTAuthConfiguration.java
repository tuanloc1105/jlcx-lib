package com.example.lcx.config;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.annotation.InstanceClass;
import vn.com.lcx.common.config.ClassPool;
import vn.com.lcx.common.utils.RSAUtils;

import java.io.IOException;

@InstanceClass
public class JWTAuthConfiguration {

    @Instance
    public JWTAuth jwtAuth() throws IOException {
        final var pub = RSAUtils.readKeyFromResource("key/key.pub", false);
        final var key = RSAUtils.readKeyFromResource("key/key.key", true);
        return JWTAuth.create(
                ClassPool.getInstance("vertx", Vertx.class),
                new JWTAuthOptions()
                        .addPubSecKey(new PubSecKeyOptions()
                                .setAlgorithm("RS256")
                                .setBuffer(pub)
                        )
                        .addPubSecKey(new PubSecKeyOptions()
                                .setAlgorithm("RS256")
                                .setBuffer(key)
                        )
        );

    }

}
