package vn.com.lcx.vertx.base.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SimpleUserAuthenticationInfo {
    private String userId;
    private String username;
    private List<String> roles;
}
