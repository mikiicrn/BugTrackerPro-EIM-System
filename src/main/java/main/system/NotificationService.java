package main.system;

import main.model.user.User;
import java.util.List;

/**
 * Service for sending notifications to users
 */
public final class NotificationService {
    private final BugTrackerSystem system;

    /**
     * Constructs a new NotificationService
     *
     * @param system The bug tracker system
     */
    public NotificationService(final BugTrackerSystem system) {
        this.system = system;
    }

    /**
     * Notifies a single user
     *
     * @param username The username of the user to notify
     * @param message  The notification message
     */
    public void notifyUser(final String username, final String message) {
        User user = system.getUser(username);
        if (user != null) {
            user.addNotification(message);
        }
    }

    /**
     * Notifies multiple users
     *
     * @param usernames A list of usernames to notify
     * @param message   The notification message
     */
    public void notifyUsers(final List<String> usernames, final String message) {
        for (String username : usernames) {
            notifyUser(username, message);
        }
    }
}
