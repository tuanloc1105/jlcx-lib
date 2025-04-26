package com.example.lcx.config;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import vn.com.lcx.common.annotation.Instance;
import vn.com.lcx.common.annotation.InstanceClass;
import vn.com.lcx.common.config.ClassPool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@InstanceClass
public class JWTAuthConfiguration {

    @Instance
    public JWTAuth jwtAuth() throws IOException {
        final var pub = readKeyFromResource("key/key.pub", "PUBLIC KEY");
        final var key = readKeyFromResource("key/key.key", "PRIVATE KEY");
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

    private static String readKeyFromResource(String resourcePath, String headerName) throws IOException {
        final var classLoader = JWTAuthConfiguration.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("File not found in resources: " + resourcePath);
            }

            byte[] keyBytes = inputStream.readAllBytes();
            String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyBytes);

            return "-----BEGIN " + headerName + "-----\n"
                    + base64
                    + "\n-----END " + headerName + "-----";
        }
    }

}
