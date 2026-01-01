package main.model.user;

import main.model.enums.Role;
import java.util.List;

public class Manager extends User {
    // The Boss
    // Creates milestones and manages developers
    private String hireDate;
    private List<String> subordinates;

    public Manager(String username, String email, String hireDate, List<String> subordinates) {
        super(username, email, Role.MANAGER);
        this.hireDate = hireDate;
        this.subordinates = subordinates;
    }

    public String getHireDate() {
        return hireDate;
    }

    public List<String> getSubordinates() {
        return subordinates;
    }
}
