package com.example.lcx.object.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.annotation.GreaterThan;
import vn.com.lcx.vertx.base.annotation.NotNull;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateTaskRequest {
    @NotNull
    @GreaterThan(0D)
    private Long id;
    @NotNull
    private String taskName;
    private String taskDetail;
    @NotNull
    private LocalDateTime remindAt;
}
