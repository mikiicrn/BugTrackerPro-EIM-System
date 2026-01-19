package main.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.model.Milestone;
import main.model.ticket.Ticket;
import main.model.user.Developer;
import main.model.user.User;
import main.model.enums.Role;

import main.model.enums.Status;
import main.model.enums.Priority;
import main.model.enums.TicketType;
import main.model.enums.Seniority;
import main.model.enums.ExpertiseArea;
import main.model.state.TicketState;
import main.model.state.StateFactory;
import main.system.BugTrackerSystem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// dealing with assignments and status changes here
public final class AssignmentCommands {
    private AssignmentCommands() {
    }

    /**
     * Assigns a ticket to a developer, checking seniority and expertise matches
     *
     * @param system The bug tracker system
     * @param user   The user performing the assignment
     * @param params The parameters for the assignment
     * @param date   The date of the assignment
     */
    public static void handleAssignTicket(final BugTrackerSystem system, final User user,
            final Map<String, Object> params,
            final LocalDate date) {
        if (!system.getState().canAssignTicket()) {
            throw new RuntimeException("Assign not allowed.");
        }
        if (user.getRole() != Role.DEVELOPER) {
            throw new RuntimeException("Only developers can assign tickets.");
        }
        int ticketId = (Integer) params.get("ticketID");
        Ticket ticket = system.getTicket(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }
        Milestone m = CommandRunner.findMilestoneForTicket(system, ticketId);
        if (m == null) {
            throw new RuntimeException("Ticket not in any milestone");
        }
        if (!m.getAssignedDevs().contains(user.getUsername())) {
            throw new RuntimeException("Developer " + user.getUsername()
                    + " is not assigned to milestone " + m.getName() + ".");
        }
        if (CommandRunner.isMilestoneBlocked(system, m)) {
            throw new RuntimeException("Cannot assign ticket " + ticketId
                    + " from blocked milestone " + m.getName() + ".");
        }

        Developer dev = (Developer) user;

        // checking the seniority level required for this priority
        boolean seniorityOk = false;
        Seniority s = dev.getSeniority();

        List<String> allowedSeniorities = new ArrayList<>();
        allowedSeniorities.add("SENIOR");

        if (ticket.getPriority() != Priority.CRITICAL) {
            allowedSeniorities.add("MID");
        }

        if ((ticket.getPriority() == Priority.LOW || ticket.getPriority() == Priority.MEDIUM)
                && (ticket.getType() == TicketType.BUG
                        || ticket.getType() == TicketType.UI_FEEDBACK)) {
            allowedSeniorities.add("JUNIOR");
        }

        Collections.sort(allowedSeniorities);

        if (allowedSeniorities.contains(s.toString())) {
            seniorityOk = true;
        }

        if (!seniorityOk) {
            String req = String.join(", ", allowedSeniorities);
            throw new RuntimeException("Developer " + dev.getUsername()
                    + " cannot assign ticket " + ticketId
                    + " due to seniority level. Required: " + req + "; Current: " + s + ".");
        }

        ExpertiseArea tArea = ticket.getExpertiseArea();
        if (tArea != null) {
            boolean expertiseOk = false;
            ExpertiseArea dArea = dev.getExpertiseArea();
            List<String> allowedExpertise = new ArrayList<>();

            // checking if the dev has the right expertise for this ticket
            allowedExpertise.add("FULLSTACK");

            if (tArea == ExpertiseArea.FRONTEND) {
                allowedExpertise.add("FRONTEND");
                allowedExpertise.add("DESIGN");
            } else if (tArea == ExpertiseArea.BACKEND) {
                allowedExpertise.add("BACKEND");
            } else if (tArea == ExpertiseArea.DB) {
                allowedExpertise.add("DB");
                allowedExpertise.add("BACKEND");
            } else if (tArea == ExpertiseArea.DESIGN) {
                allowedExpertise.add("DESIGN");
                allowedExpertise.add("FRONTEND");
            } else if (tArea == ExpertiseArea.DEVOPS) {
                allowedExpertise.add("DEVOPS");
            }

            Collections.sort(allowedExpertise);

            if (allowedExpertise.contains(dArea.toString())) {
                expertiseOk = true;
            }

            if (!expertiseOk) {
                String req = String.join(", ", allowedExpertise);
                throw new RuntimeException("Developer " + dev.getUsername()
                        + " cannot assign ticket " + ticketId
                        + " due to expertise area. Required: " + req + "; Current: " + dArea + ".");
            }
        }

        if (ticket.getStatus() != Status.OPEN) {
            throw new RuntimeException("Only OPEN tickets can be assigned.");
        }

        ticket.setAssignedTo(dev.getUsername());
        ticket.setStatus(Status.IN_PROGRESS);
        ticket.setAssignedAt(date);

        Map<String, String> data = new HashMap<>();
        data.put("username", dev.getUsername());
        CommandRunner.logHistory(ticket, "ASSIGNED", date, data);

        Map<String, String> statusData = new HashMap<>();
        statusData.put("oldStatus", Status.OPEN.toString());
        statusData.put("newStatus", Status.IN_PROGRESS.toString());
        statusData.put("username", dev.getUsername());
        CommandRunner.logHistory(ticket, "STATUS_CHANGED", date, statusData);
    }

    /**
     * Un-assigns a ticket from a developer
     *
     * @param system The bug tracker system
     * @param user   The user performing the undo action
     * @param params The parameters for the undo action
     */
    public static void handleUndoAssignTicket(final BugTrackerSystem system, final User user,
            final Map<String, Object> params) {
        int ticketId = (Integer) params.get("ticketID");
        Ticket ticket = system.getTicket(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }

        if (ticket.getStatus() != Status.IN_PROGRESS) {
            throw new RuntimeException("Ticket not IN_PROGRESS");
        }
        if (!user.getUsername().equals(ticket.getAssignedTo())) {
            throw new RuntimeException("Not assigned to you");
        }

        ticket.setAssignedTo(null);
        ticket.setAssignedAt(null);
        ticket.setStatus(Status.OPEN);

        Map<String, String> data = new HashMap<>();
        data.put("username", user.getUsername());
        CommandRunner.logHistory(ticket, "DE-ASSIGNED", system.getCurrentDate(), data);
    }

    /**
     * Changes the status of a ticket, handling side effects like unblocking
     *
     * @param system The bug tracker system
     * @param user   The user performing the status change
     * @param params The parameters for the status change
     */
    public static void handleChangeStatus(final BugTrackerSystem system, final User user,
            final Map<String, Object> params) {
        int ticketId = (Integer) params.get("ticketID");
        Ticket ticket = system.getTicket(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }

        if (!user.getUsername().equals(ticket.getAssignedTo())) {
            throw new RuntimeException("Ticket " + ticketId + " is not assigned to developer "
                    + user.getUsername() + ".");
        }
        if (ticket.getStatus() == Status.CLOSED) {
            return;
        }

        Status current = ticket.getStatus();
        // keeping track of what was blocked before this change
        Set<String> blockedBefore = new HashSet<>();
        for (Milestone m : system.getMilestones().values()) {
            if (CommandRunner.isMilestoneBlocked(system, m)) {
                blockedBefore.add(m.getName());
            }
        }

        Status next = null;
        TicketState state = StateFactory.getState(current);
        if (state != null) {
            next = state.nextStatus();
        }
        if (next != null) {
            ticket.setStatus(next);
            if (next == Status.RESOLVED) {
                ticket.setSolvedAt(system.getCurrentDate());
            } else if (next == Status.CLOSED && ticket.getSolvedAt() == null) {
                ticket.setSolvedAt(system.getCurrentDate());
            }
            Map<String, String> data = new HashMap<>();
            data.put("oldStatus", current.toString());
            data.put("newStatus", next.toString());
            data.put("username", user.getUsername());
            CommandRunner.logHistory(ticket, "STATUS_CHANGED", system.getCurrentDate(), data);

            for (Milestone m : system.getMilestones().values()) {
                if (blockedBefore.contains(m.getName())) {
                    boolean isBlockedNow = CommandRunner.isMilestoneBlocked(system, m);
                    if (!isBlockedNow) {
                        // guarding against unblocking after due date
                        if (system.getCurrentDate().isAfter(m.getDueDate())) {
                            CommandRunner.notifyUsers(system, m.getAssignedDevs(),
                                    "Milestone " + m.getName()
                                            + " was unblocked after due date. "
                                            + "All active tickets are now CRITICAL.");

                            for (int tId : m.getTickets()) {
                                Ticket t = system.getTicket(tId);
                                if (t != null && t.getStatus() != Status.CLOSED) {
                                    t.setPriority(Priority.CRITICAL);

                                    if (t.getAssignedTo() != null) {
                                        User assignedUser = system.getUser(t.getAssignedTo());
                                        if (assignedUser.getRole() == Role.DEVELOPER) {
                                            Developer dev = (Developer) assignedUser;
                                            if (!dev.canHandle(t)) {
                                                String devName = t.getAssignedTo();
                                                t.setAssignedTo(null);
                                                t.setStatus(Status.OPEN);

                                                Map<String, String> deassi = new HashMap<>();
                                                deassi.put("username", devName);
                                                deassi.put("reason", "seniority_mismatch");
                                                CommandRunner.logHistory(t, "DE-ASSIGNED",
                                                        system.getCurrentDate(),
                                                        deassi);

                                                Map<String, String> statusChange = new HashMap<>();
                                                statusChange.put("oldStatus", "IN_PROGRESS");
                                                statusChange.put("newStatus", "OPEN");
                                                CommandRunner.logHistory(t, "STATUS_CHANGED",
                                                        system.getCurrentDate(),
                                                        statusChange);
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            CommandRunner.notifyUsers(system, m.getAssignedDevs(),
                                    "Milestone " + m.getName() + " is now unblocked as tichet "
                                            + ticketId + " has been CLOSED.");
                        }
                    }
                }
            }
        }
    }

    /**
     * Reverts the last status change of a ticket
     *
     * @param system The bug tracker system
     * @param user   The user performing the undo action
     * @param params The parameters for the undo action
     */
    public static void handleUndoChangeStatus(final BugTrackerSystem system, final User user,
            final Map<String, Object> params) {
        int ticketId = (Integer) params.get("ticketID");
        Ticket ticket = system.getTicket(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Ticket not found");
        }

        if (!user.getUsername().equals(ticket.getAssignedTo())) {
            throw new RuntimeException("Ticket " + ticketId + " is not assigned to developer "
                    + user.getUsername() + ".");
        }

        if (ticket.getStatus() == Status.IN_PROGRESS) {
            return;
        }

        Status old = ticket.getStatus();
        ticket.revertStatus();
        Status current = ticket.getStatus();

        if (current == Status.OPEN || current == Status.IN_PROGRESS) {
            ticket.setSolvedAt(null);
        }

        Map<String, String> data = new HashMap<>();
        data.put("oldStatus", old.toString());
        data.put("newStatus", current.toString());
        data.put("username", user.getUsername());
        CommandRunner.logHistory(ticket, "STATUS_CHANGED", system.getCurrentDate(), data);
    }

    /**
     * Views the assigned tickets for a developer
     *
     * @param system The bug tracker system
     * @param user   The user viewing the tickets
     * @param result The output object node
     */
    public static void handleViewAssignedTickets(final BugTrackerSystem system, final User user,
            final ObjectNode result) {
        if (user.getRole() != Role.DEVELOPER) {
            throw new RuntimeException("Only developers.");
        }
        List<Ticket> assigned = new ArrayList<>();
        for (Ticket t : system.getAllTickets()) {
            if (user.getUsername().equals(t.getAssignedTo())) {
                assigned.add(t);
            }
        }
        assigned.sort((t1, t2) -> {
            int p = t2.getPriority().compareTo(t1.getPriority());
            if (p != 0) {
                return p;
            }
            int c = t1.getCreatedAt().compareTo(t2.getCreatedAt());
            if (c != 0) {
                return c;
            }
            return Integer.compare(t1.getId(), t2.getId());
        });
        ArrayNode arr = result.putArray("assignedTickets");
        for (Ticket t : assigned) {
            ObjectNode n = arr.addObject();
            n.put("id", t.getId());
            n.put("type", t.getType().toString());
            n.put("title", t.getTitle());
            n.put("businessPriority", t.getPriority().toString());
            n.put("status", t.getStatus().toString());
            if (t.getAssignedAt() != null) {
                n.put("assignedAt", t.getAssignedAt().toString());
            } else {
                n.put("assignedAt", "");
            }
            if (t.getCreatedAt() != null) {
                n.put("createdAt", t.getCreatedAt().toString());
            } else {
                n.put("createdAt", "");
            }
            n.put("reportedBy", t.getReportedBy());
            ArrayNode commentsNode = n.putArray("comments");
            for (Map<String, String> comment : t.getComments()) {
                ObjectNode cNode = commentsNode.addObject();
                cNode.put("author", comment.get("author"));
                cNode.put("content", comment.get("content"));
                cNode.put("createdAt", comment.get("createdAt"));
            }
        }
    }
}
