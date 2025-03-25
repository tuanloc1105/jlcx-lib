package com.example.lcx.http.response;

import com.example.lcx.dto.UserDTO;
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
public class UserListResponse extends CommonResponse {

    private static final long serialVersionUID = 6226575793832950987L;

    private List<UserDTO> users;

}
