package main.utils;

import main.model.enums.*;
import main.model.user.*;
import java.util.List;
import java.util.Map;

public class UserFactory {

    // Factory Pattern! I use this to create different types of users (Reporter,
    // Developer, Manager)
    // without cluttering the main code with "new Reporter(...)" everywhere

    @SuppressWarnings("unchecked")
    public static User createUser(Map<String, Object> params) {
        String roleStr = (String) params.get("role");
        Role role = Role.valueOf(roleStr);
        String username = (String) params.get("username");
        String email = (String) params.get("email");

        // Based on the role, I decide which class to instantiate
        // It's like a vending machine for User objects
        switch (role) {
            case REPORTER:
                return new Reporter(username, email);
            case DEVELOPER:
                String dHireDate = (String) params.get("hireDate");
                ExpertiseArea expertiseArea = ExpertiseArea.valueOf((String) params.get("expertiseArea"));
                Seniority seniority = Seniority.valueOf((String) params.get("seniority"));
                return new Developer(username, email, dHireDate, expertiseArea, seniority);
            case MANAGER:
                String mHireDate = (String) params.get("hireDate");
                List<String> subordinates = (List<String>) params.get("subordinates");
                return new Manager(username, email, mHireDate, subordinates);
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}
