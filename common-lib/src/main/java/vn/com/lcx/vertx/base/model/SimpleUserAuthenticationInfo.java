package vn.com.lcx.vertx.base.model;

import java.util.List;

public class SimpleUserAuthenticationInfo {
    private String userId;
    private String username;
    private List<String> roles;

    public SimpleUserAuthenticationInfo() {
    }

    public SimpleUserAuthenticationInfo(String userId, String username, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

}
