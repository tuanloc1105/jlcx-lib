package com.example.lcx.object.response;

import com.example.lcx.object.dto.ReactiveTaskDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetTaskDetailResponse2 extends CommonResponse {
    private static final long serialVersionUID = -199680635366316884L;

    private ReactiveTaskDTO task;
}
