package main.model.user;

import main.model.enums.Role;
import java.util.List;
import java.util.ArrayList;

/**
 * Abstract class representing a user in the system
 */
public abstract class User {

    private String username;
    private String email;
    private Role role;
    private List<String> notifications = new ArrayList<>();

    /**
     * Constructs a new User
     *
     * @param username The username
     * @param email    The email
     * @param role     The role of the user
     */
    public User(final String username, final String email, final Role role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    /**
     * Gets the username
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the email
     *
     * @return The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's role
     *
     * @return The role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Adds a notification to the user's list used to check notifications
     *
     * @param notification The notification message
     */
    public void addNotification(final String notification) {
        this.notifications.add(notification);
    }

    /**
     * Gets the list of notifications
     *
     * @return A copy of the notification list
     */
    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    /**
     * Clears all notifications
     */
    public void clearNotifications() {
        this.notifications.clear();
    }
}
