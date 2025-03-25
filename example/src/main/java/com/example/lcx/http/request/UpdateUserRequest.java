package com.example.lcx.http.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.lcx.vertx.base.annotation.GreaterThan;
import vn.com.lcx.vertx.base.annotation.NotNull;
import vn.com.lcx.vertx.base.http.request.BaseRequest;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateUserRequest implements BaseRequest {

    private static final long serialVersionUID = -7025340182826702910L;

    @NotNull
    @GreaterThan(value = 0D)
    private Long id;

    private String firstName;

    private String lastName;

    private Integer age;

    @Override
    public void validate() {
        
    }
}
