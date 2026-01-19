package main.utils.user;

import main.model.user.Manager;
import main.model.user.User;
import java.util.List;
import java.util.Map;

/**
 * Creator for Manager users
 */
public final class ManagerCreator implements UserCreator {
    @Override
    @SuppressWarnings("unchecked")
    public User createUser(final String username, final String email, final Map<String, Object> params) {
        String hireDate = (String) params.get("hireDate");
        List<String> subordinates = (List<String>) params.get("subordinates");
        return new Manager(username, email, hireDate, subordinates);
    }
}
