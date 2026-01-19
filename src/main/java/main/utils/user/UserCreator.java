package main.utils.user;

import main.model.user.User;
import java.util.Map;

/**
 * Interface for creating users
 */
public interface UserCreator {
    /**
     * Creates a user based on the provided parameters
     * 
     * @param username The username
     * @param email    The email
     * @param params   The map of parameters
     * @return The created User
     */
    User createUser(String username, String email, Map<String, Object> params);
}
