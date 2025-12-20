package com.example.model.http.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

import java.io.Serial;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppResponse<T> extends CommonResponse {

    @Serial
    private static final long serialVersionUID = -5694590293449614013L;

    private T data;

}
