package com.example.lcx.http.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.http.request.BaseRequest;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FindUserRequest implements BaseRequest {

    private static final long serialVersionUID = 6424226968807990819L;
    private String firstName;
    private String lastName;
    private Integer age;
    private Integer pageSize;
    private Integer pageNumber;

    @Override
    public void validate() {
        
    }
}
