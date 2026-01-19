package main.utils;

import main.model.enums.*;
import main.model.user.*;
import main.utils.user.*;
import java.util.Map;

public class UserFactory {

    private static final Map<Role, UserCreator> CREATOR_MAP = new java.util.EnumMap<>(Role.class);

    static {
        CREATOR_MAP.put(Role.REPORTER, new ReporterCreator());
        CREATOR_MAP.put(Role.DEVELOPER, new DeveloperCreator());
        CREATOR_MAP.put(Role.MANAGER, new ManagerCreator());
    }

    private UserFactory() {
    }

    /**
     * Creates a user based on parameters
     * 
     * @param params The map of parameters
     * @return The created User
     */
    public static User createUser(Map<String, Object> params) {
        String roleStr = (String) params.get("role");
        Role role = Role.valueOf(roleStr);
        String username = (String) params.get("username");
        String email = (String) params.get("email");

        UserCreator creator = CREATOR_MAP.get(role);
        if (creator == null) {
            throw new IllegalArgumentException("Unknown role: " + role);
        }
        return creator.createUser(username, email, params);
    }
}
