package main.model.user;

import main.model.enums.Role;

public abstract class User {
    // Base class for everyone using the system
    // Stores common stuff like username and email
    private String username;
    private String email;
    private Role role;

    public User(String username, String email, Role role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    private java.util.List<String> notifications = new java.util.ArrayList<>();

    public void addNotification(String notification) {
        this.notifications.add(notification);
    }

    public java.util.List<String> getNotifications() {
        return new java.util.ArrayList<>(notifications);
    }

    public void clearNotifications() {
        this.notifications.clear();
    }
}
