package com.example.lcx.object.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserLoginResponse extends CommonResponse {
    private static final long serialVersionUID = -4165450108673672703L;

    private String token;
    private String fullName;

}
