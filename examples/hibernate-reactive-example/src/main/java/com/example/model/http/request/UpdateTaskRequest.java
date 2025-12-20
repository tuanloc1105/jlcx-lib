package com.example.model.http.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.annotation.GreaterThan;
import vn.com.lcx.vertx.base.annotation.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateTaskRequest {
    @NotNull
    @GreaterThan(0D)
    private Long id;
    @NotNull
    private String taskTitle;
    private String taskDetail;
    private Boolean finished;
}
