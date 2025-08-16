package com.example.lcx.object.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.annotation.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserLoginRequest {
    @NotNull
    private String username;
    @NotNull
    private String password;
}
