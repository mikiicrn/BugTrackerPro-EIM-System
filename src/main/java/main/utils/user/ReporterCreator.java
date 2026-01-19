package main.utils.user;

import main.model.user.Reporter;
import main.model.user.User;
import java.util.Map;

/**
 * Creator for Reporter users
 */
public final class ReporterCreator implements UserCreator {
    @Override
    public User createUser(final String username, final String email, final Map<String, Object> params) {
        return new Reporter(username, email);
    }
}
