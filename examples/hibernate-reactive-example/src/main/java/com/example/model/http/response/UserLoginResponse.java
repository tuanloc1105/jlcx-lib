package com.example.model.http.response;

import com.example.model.dto.UsersDTO;
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
public class UserLoginResponse extends CommonResponse {

    @Serial
    private static final long serialVersionUID = -3961342628864863712L;

    private String token;
    private UsersDTO userInfo;

}
