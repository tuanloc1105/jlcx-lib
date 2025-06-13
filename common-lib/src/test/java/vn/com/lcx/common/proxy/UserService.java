package vn.com.lcx.common.proxy;

/**
 * A concrete class (not implementing any interface) for testing class proxying.
 */
public class UserService {
    private int userCount = 0;
    private String name;

    public UserService(String name) {
        this.name = name;
    }

    public UserService() {
    }

    public String createUser(String username, String email) {
        userCount++;
        return String.format("User %s (%s) created. Total users: %d", username, email, userCount);
    }

    public String deleteUser(String username) {
        if (userCount > 0) {
            userCount--;
            return String.format("User %s deleted. Remaining users: %d", username, userCount);
        }
        return "No users to delete";
    }

    public int getUserCount() {
        return userCount;
    }

    public void testException() {
        throw new UserException("test exception");
    }
}
