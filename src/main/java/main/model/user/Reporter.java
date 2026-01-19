package main.model.user;

import main.model.enums.Role;

/**
 * Regular user or client who finds bugs. Can only report tickets
 */
public class Reporter extends User {
    /**
     * Constructs a new Reporter
     *
     * @param username The reporter's username
     * @param email    The reporter's email
     */
    public Reporter(final String username, final String email) {
        super(username, email, Role.REPORTER);
    }
}
