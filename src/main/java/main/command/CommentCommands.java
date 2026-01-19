package main.command;

import main.model.enums.Role;
import main.model.enums.Status;
import main.model.enums.TicketType;
import main.model.ticket.Ticket;
import main.model.user.User;
import main.system.BugTrackerSystem;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// logic for adding comments
public final class CommentCommands {
    private CommentCommands() {
    }

    /**
     * Adds a comment to a ticket after validation
     *
     * @param system The bug tracker system
     * @param user   The user adding the comment
     * @param params The parameters for the comment
     * @param date   The date of the comment
     */
    @SuppressWarnings("unchecked")
    public static void handleAddComment(final BugTrackerSystem system, final User user,
            final Map<String, Object> params,
            final LocalDate date) {
        Map<String, Object> p = params;
        if (params.containsKey("params")) {
            p = (Map<String, Object>) params.get("params");
        }

        Integer tid = getTicketId(p);
        if (tid == null) {
            return;
        }

        String content = (String) p.get("comment");

        Ticket t = system.getTicket(tid);
        if (t == null) {
            return;
        }

        if (t.getType() == TicketType.BUG
                && (t.getReportedBy() == null || t.getReportedBy().isEmpty())) {
            throw new RuntimeException("Comments are not allowed on anonymous tickets.");
        }

        final int minLength = 10;
        if (content == null || content.length() < minLength) {
            throw new RuntimeException("Comment must be at least 10 characters long.");
        }

        if (user.getRole() == Role.REPORTER) {
            if (t.getStatus() == Status.CLOSED) {
                throw new RuntimeException("Reporters cannot comment on CLOSED tickets.");
            }
            if (!user.getUsername().equals(t.getReportedBy())) {
                throw new RuntimeException("Reporter " + user.getUsername()
                        + " cannot comment on ticket " + t.getId() + ".");
            }
        } else if (user.getRole() == Role.DEVELOPER) {
            if (!user.getUsername().equals(t.getAssignedTo())) {
                throw new RuntimeException("Ticket " + t.getId()
                        + " is not assigned to the developer " + user.getUsername() + ".");
            }
        }

        Map<String, String> comment = new HashMap<>();
        comment.put("author", user.getUsername());
        comment.put("content", content);
        comment.put("createdAt", date.toString());
        t.addComment(comment);
    }

    /**
     * Undoes the last comment action
     *
     * @param system The bug tracker system
     * @param user   The user undoing the comment
     * @param p      The parameters
     */
    @SuppressWarnings("unchecked")
    public static void handleUndoAddComment(final BugTrackerSystem system, final User user,
            final Map<String, Object> p) {
        Map<String, Object> params = p;
        if (p.containsKey("params")) {
            params = (Map<String, Object>) p.get("params");
        }

        Integer tid = getTicketId(params);
        if (tid == null) {
            return;
        }

        Ticket t = system.getTicket(tid);
        if (t == null) {
            return;
        }

        if (t.getType() == TicketType.BUG
                && (t.getReportedBy() == null || t.getReportedBy().isEmpty())) {
            throw new RuntimeException("Comments are not allowed on anonymous tickets.");
        }

        List<Map<String, String>> c = t.getComments();
        if (c.isEmpty()) {
            return;
        }

        Map<String, String> last = c.get(c.size() - 1);
        if (last.get("author").equals(user.getUsername())) {
            t.removeLastComment();
        }
    }

    private static Integer getTicketId(final Map<String, Object> params) {
        if (params.containsKey("ticketId")) {
            Object val = params.get("ticketId");
            if (val != null) {
                try {
                    return (Integer) val;
                } catch (ClassCastException e) {
                    // ignore
                }
            }
        }
        if (params.containsKey("ticketID")) {
            Object val = params.get("ticketID");
            if (val != null) {
                try {
                    return (Integer) val;
                } catch (ClassCastException e) {
                    // ignore
                }
            }
        }
        return null;
    }
}
