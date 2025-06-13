package vn.com.lcx.common.proxy;

public interface UserService2 {
    String createUser(String username, String email);
    String deleteUser(String username);
    int getUserCount();
    void testException();
}
