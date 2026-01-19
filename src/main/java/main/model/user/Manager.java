package main.model.user;

import main.model.enums.Role;
import java.util.List;

/**
 * The Boss. Creates milestones and manages developers
 */
public class Manager extends User {
    private String hireDate;
    private List<String> subordinates;

    /**
     * Constructs a new Manager
     *
     * @param username     The manager's username
     * @param email        The manager's email
     * @param hireDate     The hire date string
     * @param subordinates The list of subordinate usernames
     */
    public Manager(final String username, final String email, final String hireDate,
            final List<String> subordinates) {
        super(username, email, Role.MANAGER);
        this.hireDate = hireDate;
        this.subordinates = subordinates;
    }

    /**
     * Gets the hire date
     *
     * @return The hire date
     */
    public String getHireDate() {
        return hireDate;
    }

    /**
     * Gets the list of subordinates
     *
     * @return The list of subordinates (usernames)
     */
    public List<String> getSubordinates() {
        return subordinates;
    }
}
