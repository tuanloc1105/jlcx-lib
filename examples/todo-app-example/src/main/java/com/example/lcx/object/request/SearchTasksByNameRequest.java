package com.example.lcx.object.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.annotation.GreaterThan;
import vn.com.lcx.vertx.base.annotation.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchTasksByNameRequest {
    @NotNull
    private String searchContent;
    @NotNull
    @GreaterThan(value = 0D)
    private Integer pageNumber;
}
