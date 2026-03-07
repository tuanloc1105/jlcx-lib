package com.example.model.http.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.lcx.vertx.base.annotation.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateNewUserRequest {
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String fullName;
}
