package main.utils.user;

import main.model.enums.ExpertiseArea;
import main.model.enums.Seniority;
import main.model.user.Developer;
import main.model.user.User;
import java.util.Map;

/**
 * Creator for Developer users
 */
public final class DeveloperCreator implements UserCreator {
    @Override
    public User createUser(final String username, final String email, final Map<String, Object> params) {
        String hireDate = (String) params.get("hireDate");
        ExpertiseArea expertiseArea = ExpertiseArea.valueOf((String) params.get("expertiseArea"));
        Seniority seniority = Seniority.valueOf((String) params.get("seniority"));
        return new Developer(username, email, hireDate, expertiseArea, seniority);
    }
}
