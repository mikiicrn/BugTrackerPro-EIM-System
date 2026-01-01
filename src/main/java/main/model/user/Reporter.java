package main.model.user;

import main.model.enums.Role;

public class Reporter extends User {
    // Regular user or client who finds bugs
    // Can only report tickets
    public Reporter(String username, String email) {
        super(username, email, Role.REPORTER);
    }
}
