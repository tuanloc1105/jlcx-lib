package com.example.lcx.object.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.annotation.GreaterThan;
import vn.com.lcx.vertx.base.annotation.NotNull;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeleteTasksRequest {
    @NotNull
    @GreaterThan(0D)
    private List<Long> id;
}
