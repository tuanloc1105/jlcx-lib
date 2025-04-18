package com.example.lcx.http.response;

import com.example.lcx.dto.UserDTO;
import com.example.lcx.entity.User;
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
public class FindUserResponse extends CommonResponse {

    private static final long serialVersionUID = 1385600692894395515L;

    private Page<User> userPage;
}
