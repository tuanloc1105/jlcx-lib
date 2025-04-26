package com.example.lcx.object.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.annotation.NotNull;
import vn.com.lcx.vertx.base.http.request.BaseRequest;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateTaskRequest {
    @NotNull
    private String taskName;
    private String taskDetail;
    @NotNull
    private LocalDateTime remindAt;
}
