package com.example.lcx.object.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.annotation.GreaterThan;
import vn.com.lcx.vertx.base.annotation.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeleteTaskRequest {
    @NotNull
    @GreaterThan(0D)
    private Long id;
}
