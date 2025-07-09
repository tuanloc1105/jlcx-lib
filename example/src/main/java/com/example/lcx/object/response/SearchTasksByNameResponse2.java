package com.example.lcx.object.response;

import com.example.lcx.object.dto.ReactiveTaskDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.com.lcx.common.database.pageable.Page;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchTasksByNameResponse2 extends CommonResponse {
    private static final long serialVersionUID = 6349372107723396610L;

    private Page<ReactiveTaskDTO> tasks;
}
