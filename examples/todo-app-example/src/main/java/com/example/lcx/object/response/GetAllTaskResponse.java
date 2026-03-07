package com.example.lcx.object.response;

import com.example.lcx.object.dto.TaskDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.io.lcx.common.database.pageable.Page;
import vn.io.lcx.vertx.base.http.response.CommonResponse;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetAllTaskResponse extends CommonResponse {
    private static final long serialVersionUID = 7076999466751615893L;

    private Page<TaskDTO> tasks;
}
