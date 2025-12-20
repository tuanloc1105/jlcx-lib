package com.example.model.http.response;

import com.example.model.dto.UsersDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserLoginResponse {

    private String token;
    private UsersDTO userInfo;

}
