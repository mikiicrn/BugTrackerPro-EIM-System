package main.command;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.model.Milestone;
import main.model.ticket.Ticket;
import main.model.user.User;
import main.model.enums.Role;
import main.model.enums.Status;
import main.model.enums.Priority;
import main.system.BugTrackerSystem;
import main.utils.TicketFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Managing tickets creation and history
 */
public final class TicketCommands {

    private TicketCommands() {
    }

    /**
     * Creating a ticket, also checking for anonymous bugs
     *
     * @param system The bug tracker system
     * @param user   The user reporting the ticket
     * @param params The ticket parameters
     * @param date   The reporting date
     */
    @SuppressWarnings("unchecked")
    public static void handleReportTicket(final BugTrackerSystem system, final User user,
            final Map<String, Object> params, final LocalDate date) {
        if (!"TESTING".equals(system.getState().getName())) {
            throw new RuntimeException("Tickets can only be reported during testing phases.");
        }

        Map<String, Object> ticketData = params;
        if (params.containsKey("params") && params.get("params") != null) {
            try {
                ticketData = (Map<String, Object>) params.get("params");
            } catch (ClassCastException e) {
                // ignore
            }
        }

        String reportedBy = (String) ticketData.get("reportedBy");
        boolean isAnonymous = (reportedBy == null || reportedBy.trim().isEmpty());
        String typeStr = (String) ticketData.get("type");

        if (isAnonymous && !"BUG".equals(typeStr)) {
            throw new RuntimeException(
                    "Anonymous reports are only allowed for tickets of type BUG.");
        }

        int id = system.getNextTicketId();
        Ticket ticket = TicketFactory.createTicket(id, ticketData);
        ticket.setCreatedAt(date);
        ticket.setStatus(Status.OPEN);

        if (isAnonymous) {
            ticket.setPriority(Priority.LOW);
        }

        system.addTicket(ticket);
    }

    /**
     * View tickets visible to the user
     *
     * @param system The bug tracker system
     * @param user   The user viewing the tickets
     * @param result The result node
     */
    public static void handleViewTickets(final BugTrackerSystem system, final User user,
            final ObjectNode result) {
        List<Ticket> visibleTickets = new ArrayList<>();

        if (user.getRole() == Role.MANAGER) {
            visibleTickets.addAll(system.getAllTickets());
        } else if (user.getRole() == Role.REPORTER) {
            for (Ticket t : system.getAllTickets()) {
                if (user.getUsername().equals(t.getReportedBy())) {
                    visibleTickets.add(t);
                }
            }
        } else if (user.getRole() == Role.DEVELOPER) {
            for (Milestone m : system.getMilestones().values()) {
                if (m.getAssignedDevs().contains(user.getUsername())) {
                    for (int tId : m.getTickets()) {
                        Ticket t = system.getTicket(tId);
                        if (t != null && t.getStatus() == Status.OPEN) {
                            if (!visibleTickets.contains(t)) {
                                visibleTickets.add(t);
                            }
                        }
                    }
                }
            }
        }

        visibleTickets.sort(
                Comparator.comparing(Ticket::getCreatedAt).thenComparingInt(Ticket::getId));

        ArrayNode ticketsArray = result.putArray("tickets");
        for (Ticket t : visibleTickets) {
            ObjectNode tNode = ticketsArray.addObject();
            tNode.put("id", t.getId());
            tNode.put("type", t.getType().toString());
            tNode.put("title", t.getTitle());
            tNode.put("businessPriority", t.getPriority().toString());
            tNode.put("status", t.getStatus().toString());
            tNode.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : "");

            tNode.put("solvedAt", t.getSolvedAt() != null ? t.getSolvedAt().toString() : "");
            tNode.put("assignedAt", t.getAssignedAt() != null ? t.getAssignedAt().toString() : "");
            tNode.put("assignedTo", t.getAssignedTo() == null ? "" : t.getAssignedTo());
            tNode.put("reportedBy", t.getReportedBy());

            ArrayNode commentsNode = tNode.putArray("comments");
            for (Map<String, String> comment : t.getComments()) {
                ObjectNode cNode = commentsNode.addObject();
                cNode.put("author", comment.get("author"));
                cNode.put("content", comment.get("content"));
                cNode.put("createdAt", comment.get("createdAt"));
            }
        }
    }

    /**
     * View history of tickets
     *
     * @param system The bug tracker system
     * @param user   The user viewing the history
     * @param params The view parameters
     * @param result The result node
     */
    public static void handleViewTicketHistory(final BugTrackerSystem system, final User user,
            final Map<String, Object> params, final ObjectNode result) {
        int ticketId = -1;

        if (params.containsKey("ticketId")) {
            ticketId = (Integer) params.get("ticketId");
        } else if (params.containsKey("params")
                && ((Map<?, ?>) params.get("params")).containsKey("ticketId")) {
            ticketId = (Integer) ((Map<?, ?>) params.get("params")).get("ticketId");
        }

        List<Ticket> targets = getTargets(system, user, ticketId);
        targets.sort(Comparator.comparing(Ticket::getCreatedAt).thenComparingInt(Ticket::getId));

        ArrayNode historyArr = result.putArray("ticketHistory");
        for (Ticket t : targets) {
            ObjectNode tNode = historyArr.addObject();
            tNode.put("id", t.getId());
            tNode.put("title", t.getTitle());
            tNode.put("status", t.getStatus().toString());
            ArrayNode entries = tNode.putArray("actions");

            LocalDate unassignedDate = null;
            if (user.getRole() == Role.DEVELOPER) {
                for (Ticket.HistoryEntry h : t.getHistory()) {
                    if ("DE-ASSIGNED".equals(h.getType())
                            && user.getUsername().equals(h.getData().get("username"))) {
                        unassignedDate = h.getDate();
                    }
                }
            }

            for (Ticket.HistoryEntry h : t.getHistory()) {
                if (user.getRole() == Role.DEVELOPER && unassignedDate != null) {
                    if (h.getDate().isAfter(unassignedDate)) {
                        continue;
                    }
                }

                ObjectNode hNode = entries.addObject();
                hNode.put("timestamp", h.getDate().toString());
                hNode.put("action", h.getType());

                Map<String, String> data = h.getData();
                if (data != null) {
                    if (data.containsKey("username")) {
                        hNode.put("by", data.get("username"));
                    }
                    if (data.containsKey("milestone")) {
                        hNode.put("milestone", data.get("milestone"));
                    }
                    if (data.containsKey("oldStatus")) {
                        hNode.put("from", data.get("oldStatus"));
                    }
                    if (data.containsKey("newStatus")) {
                        hNode.put("to", data.get("newStatus"));
                    }
                }
            }

            ArrayNode commentsNode = tNode.putArray("comments");
            for (Map<String, String> c : t.getComments()) {
                ObjectNode cNode = commentsNode.addObject();
                cNode.put("author", c.get("author"));
                cNode.put("content", c.get("content"));
                cNode.put("createdAt", c.get("createdAt"));
            }
        }
    }

    private static List<Ticket> getTargets(final BugTrackerSystem system, final User user,
            final int ticketId) {
        List<Ticket> targets = new ArrayList<>();
        if (ticketId != -1) {
            Ticket t = system.getTicket(ticketId);
            if (t != null) {
                targets.add(t);
            }
        } else {
            if (user.getRole() == Role.MANAGER) {
                List<Milestone> myMilestones = new ArrayList<>();
                for (Milestone m : system.getMilestones().values()) {
                    if (m.getCreator().equals(user.getUsername())) {
                        myMilestones.add(m);
                    }
                }
                for (Milestone m : myMilestones) {
                    for (int tid : m.getTickets()) {
                        Ticket t = system.getTicket(tid);
                        if (!targets.contains(t)) {
                            targets.add(t);
                        }
                    }
                }
            } else if (user.getRole() == Role.DEVELOPER) {
                for (Ticket t : system.getAllTickets()) {
                    boolean relevant = false;
                    if (user.getUsername().equals(t.getAssignedTo())) {
                        relevant = true;
                    } else {
                        for (Ticket.HistoryEntry h : t.getHistory()) {
                            if ("ASSIGNED".equals(h.getType())
                                    && user.getUsername().equals(h.getData().get("username"))) {
                                relevant = true;
                                break;
                            }
                        }
                    }
                    if (relevant) {
                        targets.add(t);
                    }
                }
            }
        }
        return targets;
    }

}
