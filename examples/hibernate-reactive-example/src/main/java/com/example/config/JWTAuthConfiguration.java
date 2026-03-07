package com.example.config;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import lombok.RequiredArgsConstructor;
import vn.io.lcx.common.annotation.Component;
import vn.io.lcx.common.annotation.Instance;
import vn.io.lcx.common.utils.RSAUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthConfiguration {

    private final Vertx vertx;

    @Instance
    public JWTAuth jwtAuth() throws IOException {
        final var pub = RSAUtils.readKeyFromResource("key/key.pub", false);
        final var key = RSAUtils.readKeyFromResource("key/key.key", true);
        return JWTAuth.create(
                vertx,
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
