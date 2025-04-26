package com.example.lcx.object.response;

import com.example.lcx.object.dto.TaskDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SearchTasksByNameResponse extends CommonResponse {
    private static final long serialVersionUID = 6349372107723396610L;

    private List<TaskDTO> tasks;
}
