package com.example.model.http.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.io.lcx.vertx.base.annotation.GreaterThan;
import vn.io.lcx.vertx.base.annotation.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeleteTaskRequest {

    @NotNull
    @GreaterThan(0D)
    private Long id;

}
