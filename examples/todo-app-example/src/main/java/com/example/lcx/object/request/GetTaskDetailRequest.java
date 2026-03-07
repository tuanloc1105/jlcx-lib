package com.example.lcx.object.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.lcx.vertx.base.annotation.GreaterThan;
import vn.io.lcx.vertx.base.annotation.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GetTaskDetailRequest {
    @NotNull
    @GreaterThan(0D)
    private Long id;
}
