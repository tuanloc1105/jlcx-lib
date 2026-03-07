package com.example.lcx.object.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.lcx.vertx.base.annotation.GreaterThan;
import vn.io.lcx.vertx.base.annotation.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetAllTaskRequest {
    @NotNull
    @GreaterThan(value = 0D)
    private Integer pageNumber;
}
