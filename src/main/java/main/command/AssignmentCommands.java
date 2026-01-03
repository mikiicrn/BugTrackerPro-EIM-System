package main.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.model.Milestone;
import main.model.ticket.Ticket;
import main.model.user.Developer;
import main.model.user.User;

import main.model.enums.Status;
import main.model.enums.Priority;
import main.model.enums.TicketType;
import main.model.enums.Seniority;
import main.model.enums.ExpertiseArea;
import main.system.BugTrackerSystem;
import java.time.LocalDate;
import java.util.*;

// dealing with assignments and status changes here
public class AssignmentCommands {

    // assigning a ticket to a dev, i need to check seniority and expertise matches
    public static void handleAssignTicket(BugTrackerSystem system, User user, Map<String, Object> params,
            LocalDate date) {
        if (!system.getState().canAssignTicket())
            throw new RuntimeException("Assign not allowed.");
        if (!(user instanceof Developer))
            throw new RuntimeException("Only developers can assign tickets.");

        int ticketId = (Integer) params.get("ticketID");
        Ticket ticket = system.getTicket(ticketId);
        if (ticket == null)
            throw new RuntimeException("Ticket not found");

        Milestone m = CommandRunner.findMilestoneForTicket(system, ticketId);
        if (m == null)
            throw new RuntimeException("Ticket not in any milestone");

        if (!m.getAssignedDevs().contains(user.getUsername()))
            throw new RuntimeException(
                    "Developer " + user.getUsername() + " is not assigned to milestone " + m.getName() + ".");

        if (CommandRunner.isMilestoneBlocked(system, m))
            throw new RuntimeException(
                    "Cannot assign ticket " + ticketId + " from blocked milestone " + m.getName() + ".");

        Developer dev = (Developer) user;

        // checking the seniority level required for this priority
        boolean seniorityOk = false;
        Seniority s = dev.getSeniority();

        List<String> allowedSeniorities = new ArrayList<>();
        allowedSeniorities.add("SENIOR");

        if (ticket.getPriority() != Priority.CRITICAL) {
            allowedSeniorities.add("MID");
        }

        if ((ticket.getPriority() == Priority.LOW || ticket.getPriority() == Priority.MEDIUM) &&
                (ticket.getType() == TicketType.BUG || ticket.getType() == TicketType.UI_FEEDBACK)) {
            allowedSeniorities.add("JUNIOR");
        }

        Collections.sort(allowedSeniorities);

        if (allowedSeniorities.contains(s.toString())) {
            seniorityOk = true;
        }

        if (!seniorityOk) {
            String req = String.join(", ", allowedSeniorities);
            throw new RuntimeException("Developer " + dev.getUsername() + " cannot assign ticket " + ticketId +
                    " due to seniority level. Required: " + req + "; Current: " + s + ".");
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
                throw new RuntimeException("Developer " + dev.getUsername() + " cannot assign ticket " + ticketId +
                        " due to expertise area. Required: " + req + "; Current: " + dArea + ".");
            }
        }

        if (ticket.getStatus() != Status.OPEN)
            throw new RuntimeException("Only OPEN tickets can be assigned.");

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

    public static void handleUndoAssignTicket(BugTrackerSystem system, User user, Map<String, Object> params) {
        int ticketId = (Integer) params.get("ticketID");
        Ticket ticket = system.getTicket(ticketId);
        if (ticket == null)
            throw new RuntimeException("Ticket not found");

        if (ticket.getStatus() != Status.IN_PROGRESS)
            throw new RuntimeException("Ticket not IN_PROGRESS");
        if (!user.getUsername().equals(ticket.getAssignedTo()))
            throw new RuntimeException("Not assigned to you");

        ticket.setAssignedTo(null);
        ticket.setAssignedAt(null);
        ticket.setStatus(Status.OPEN);

        Map<String, String> data = new HashMap<>();
        data.put("username", user.getUsername());
        CommandRunner.logHistory(ticket, "DE-ASSIGNED", system.getCurrentDate(), data);
    }

    // changing ticket status, also handling the side effects like unblocking
    public static void handleChangeStatus(BugTrackerSystem system, User user, Map<String, Object> params) {
        int ticketId = (Integer) params.get("ticketID");
        Ticket ticket = system.getTicket(ticketId);
        if (ticket == null)
            throw new RuntimeException("Ticket not found");

        if (!user.getUsername().equals(ticket.getAssignedTo()))
            throw new RuntimeException(
                    "Ticket " + ticketId + " is not assigned to developer " + user.getUsername() + ".");
        if (ticket.getStatus() == Status.CLOSED)
            return;

        Status current = ticket.getStatus();
        // keeping track of what was blocked before this change
        Set<String> blockedBefore = new HashSet<>();
        for (Milestone m : system.getMilestones().values()) {
            if (CommandRunner.isMilestoneBlocked(system, m)) {
                blockedBefore.add(m.getName());
            }
        }

        Status next = null;
        switch (current) {
            case OPEN:
                next = Status.IN_PROGRESS;
                break;
            case IN_PROGRESS:
                next = Status.RESOLVED;
                break;
            case RESOLVED:
                next = Status.CLOSED;
                break;
            case CLOSED:
                break;
        }
        if (next != null) {
            ticket.setStatus(next);
            if (next == Status.RESOLVED || next == Status.CLOSED) {
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
                            CommandRunner.notifyUsers(system, m.getAssignedDevs(), "Milestone " + m.getName()
                                    + " was unblocked after due date. All active tickets are now CRITICAL.");

                            for (int tId : m.getTickets()) {
                                Ticket t = system.getTicket(tId);
                                if (t != null && t.getStatus() != Status.CLOSED) {
                                    t.setPriority(Priority.CRITICAL);

                                    if (t.getAssignedTo() != null) {
                                        User assignedUser = system.getUser(t.getAssignedTo());
                                        if (assignedUser instanceof Developer) {
                                            Developer dev = (Developer) assignedUser;
                                            if (!dev.canHandle(t)) {
                                                String devName = t.getAssignedTo();
                                                t.setAssignedTo(null);
                                                t.setStatus(Status.OPEN);

                                                Map<String, String> deassignData = new HashMap<>();
                                                deassignData.put("username", devName);
                                                deassignData.put("reason", "seniority_mismatch");
                                                CommandRunner.logHistory(t, "DE-ASSIGNED", system.getCurrentDate(),
                                                        deassignData);

                                                Map<String, String> statusChangeData = new HashMap<>();
                                                statusChangeData.put("oldStatus", "IN_PROGRESS");
                                                statusChangeData.put("newStatus", "OPEN");
                                                CommandRunner.logHistory(t, "STATUS_CHANGED", system.getCurrentDate(),
                                                        statusChangeData);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void handleUndoChangeStatus(BugTrackerSystem system, User user, Map<String, Object> params) {
        int ticketId = (Integer) params.get("ticketID");
        Ticket ticket = system.getTicket(ticketId);
        if (ticket == null)
            throw new RuntimeException("Ticket not found");

        if (!user.getUsername().equals(ticket.getAssignedTo()))
            throw new RuntimeException(
                    "Ticket " + ticketId + " is not assigned to developer " + user.getUsername() + ".");

        if (ticket.getStatus() == Status.IN_PROGRESS)
            return;

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

    public static void handleViewAssignedTickets(BugTrackerSystem system, User user, ObjectNode result) {
        if (!(user instanceof Developer))
            throw new RuntimeException("Only developers.");

        List<Ticket> assigned = new ArrayList<>();
        for (Ticket t : system.getAllTickets()) {
            if (user.getUsername().equals(t.getAssignedTo())) {
                assigned.add(t);
            }
        }

        assigned.sort((t1, t2) -> {
            int p = t2.getPriority().compareTo(t1.getPriority());
            if (p != 0)
                return p;
            int c = t1.getCreatedAt().compareTo(t2.getCreatedAt());
            if (c != 0)
                return c;
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
            n.put("assignedAt", t.getAssignedAt() != null ? t.getAssignedAt().toString() : "");
            n.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : "");
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
