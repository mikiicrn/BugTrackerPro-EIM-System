package main.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.model.Milestone;
import main.model.enums.Priority;
import main.model.enums.Role;
import main.model.enums.Status;
import main.model.ticket.Ticket;
import main.model.user.Developer;
import main.model.user.User;
import main.system.BugTrackerSystem;
import main.system.state.DevelopmentState;
import main.system.state.TestingState;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// i use this to run the commands from local inputs
public final class CommandRunner {

    private CommandRunner() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Executes a command
     *
     * @param command   The name of the command to execute
     * @param username  The username of the user executing the command
     * @param timestamp The timestamp of the command execution
     * @param params    The parameters for the command
     */
    @SuppressWarnings("unchecked")
    public static void execute(final String command, final String username,
            final String timestamp, final Map<String, Object> params) {
        BugTrackerSystem system = BugTrackerSystem.getInstance();
        if (!system.isActive()) {
            return;
        }

        LocalDate date = LocalDate.parse(timestamp);

        updateTime(system, date);

        User user = system.getUser(username);
        ObjectNode result = MAPPER.createObjectNode();
        result.put("command", command);
        result.put("timestamp", timestamp);

        if (username != null) {
            result.put("username", username);
        }

        try {
            if (user == null) {
                // checking if this is an anonymous bug report
                boolean isAnon = false;
                if ("reportTicket".equals(command)) {
                    Map<String, Object> p = params;
                    if (params != null && params.containsKey("params")) {
                        p = (Map<String, Object>) params.get("params");
                    }
                    if (p != null) {
                        String type = (String) p.get("type");
                        String repBy = (String) p.get("reportedBy");
                        if ("BUG".equals(type) && (repBy == null || repBy.isEmpty())) {
                            isAnon = true;
                        }
                    }
                }

                if (!isAnon) {
                    throw new RuntimeException("The user " + username + " does not exist.");
                }
            }

            switch (command) {
                case "reportTicket":
                    TicketCommands.handleReportTicket(system, user, params, date);
                    break;
                case "viewTickets":
                    TicketCommands.handleViewTickets(system, user, result);
                    break;
                case "createMilestone":
                    MilestoneCommands.handleCreateMilestone(system, user, params, date);
                    break;
                case "assignTicket":
                    AssignmentCommands.handleAssignTicket(system, user, params, date);
                    break;
                case "undoAssignTicket":
                    AssignmentCommands.handleUndoAssignTicket(system, user, params);
                    break;
                case "changeStatus":
                    AssignmentCommands.handleChangeStatus(system, user, params);
                    break;
                case "undoChangeStatus":
                    AssignmentCommands.handleUndoChangeStatus(system, user, params);
                    break;
                case "viewAssignedTickets":
                    AssignmentCommands.handleViewAssignedTickets(system, user, result);
                    break;
                case "viewMilestones":
                    MilestoneCommands.handleViewMilestones(system, user, result);
                    break;
                case "viewTicketHistory":
                    TicketCommands.handleViewTicketHistory(system, user, params, result);
                    break;
                case "startTestingPhase":
                    handleStartTestingPhase(system, user);
                    break;
                case "lostInvestors":
                    if (user.getRole() != Role.MANAGER) {
                        throw new RuntimeException("Only Managers can declare lost investors.");
                    }
                    system.setActive(false);
                    break;
                case "addComment":
                    CommentCommands.handleAddComment(system, user, params, date);
                    break;
                case "undoAddComment":
                    CommentCommands.handleUndoAddComment(system, user, params);
                    break;
                case "viewNotifications":
                    handleViewNotifications(system, user, result);
                    break;
                case "search":
                    SearchCommands.handleSearch(system, user, params, result);
                    break;
                case "generatePerformanceReport":
                    ReportCommands.handleGeneratePerformanceReport(system, user, params, result);
                    break;
                case "generateTicketRiskReport":
                    ReportCommands.handleGenerateTicketRiskReport(system, user, result);
                    break;
                case "generateResolutionEfficiencyReport":
                    ReportCommands.handleGenerateResolutionEfficiencyReport(system, user, result);
                    break;
                case "generateCustomerImpactReport":
                    ReportCommands.handleGenerateCustomerImpactReport(system, user, result);
                    break;
                case "appStabilityReport":
                    ReportCommands.handleAppStabilityReport(system, user, result);
                    break;
                default:
                    result.put("status", "error");
                    result.put("message", "Unknown command: " + command);
                    break;
            }
        } catch (Exception e) {
            String c = command;
            if ("reportTicket".equals(c) || "createMilestone".equals(c) || "assignTicket".equals(c)
                    || "addComment".equals(c) || "undoAddComment".equals(c)
                    || "changeStatus".equals(c) || "undoChangeStatus".equals(c)) {
                result.put("error", e.getMessage());
            } else {
                ObjectNode errorNode = result.putObject("output");
                errorNode.put("status", "error");
                errorNode.put("message", e.getMessage());
            }
        }

        if (result.has("output") || result.has("tickets")
                || result.has("milestones") || result.has("notifications")
                || result.has("history") || result.has("assignedTickets")
                || result.has("error") || result.has("report")
                || result.has("riskReport") || result.has("efficiencyReport")
                || result.has("customerImpact")
                || result.has("stability") || result.has("developers")
                || result.has("ticketHistory")
                || result.has("results")) {
            system.addOutput(result);
        }
    }

    /**
     * Logs an action in the ticket's history
     *
     * @param ticket The ticket to log the history for
     * @param type   The type of action
     * @param date   The date of the action
     * @param data   Additional data for the log
     */
    public static void logHistory(final Ticket ticket, final String type,
            final LocalDate date,
            final Map<String, String> data) {
        ticket.addHistory(type, date, data);
    }

    /**
     * Notifies a list of users
     *
     * @param system    The bug tracker system
     * @param usernames The list of usernames to notify
     * @param message   The notification message
     */
    public static void notifyUsers(final BugTrackerSystem system,
            final List<String> usernames,
            final String message) {
        for (String u : usernames) {
            User user = system.getUser(u);
            if (user != null) {
                user.addNotification(message);
            }
        }
    }

    /**
     * Updates the system time and checks for testing phase transitions or milestone
     * deadlines
     *
     * @param system  The bug tracker system
     * @param newDate The new date to update to
     */
    private static void updateTime(final BugTrackerSystem system, final LocalDate newDate) {
        LocalDate currentDate = system.getCurrentDate();
        if (currentDate == null) {
            system.setCurrentDate(newDate);
            if (system.getState() == null) {
                system.setState(new TestingState());
                system.setPhaseStartDate(newDate);
            }
            return;
        }

        if (newDate.isEqual(currentDate)) {
            return;
        }

        if ("TESTING".equals(system.getState().getName())) {
            long daysInPhase = ChronoUnit.DAYS.between(system.getPhaseStartDate(), newDate);
            final int testingDays = 12;
            if (daysInPhase >= testingDays) {
                system.setState(new DevelopmentState());
            }
        }

        system.setCurrentDate(newDate);

        for (Milestone m : system.getMilestones().values()) {
            boolean isBlocked = isMilestoneBlocked(system, m);

            long daysSinceCreation = ChronoUnit.DAYS.between(m.getCreationDate(), newDate);

            final int daysForIncrement = 3;
            int requiredIncrements = (int) (daysSinceCreation / daysForIncrement);

            if (requiredIncrements > m.getPriorityIncrementsApplied()) {
                if (!isBlocked) {
                    int pending = requiredIncrements - m.getPriorityIncrementsApplied();
                    for (int tId : m.getTickets()) {
                        Ticket t = system.getTicket(tId);
                        if (t != null && t.getStatus() != Status.CLOSED) {
                            for (int i = 0; i < pending; i++) {
                                if (t.getPriority() != Priority.CRITICAL) {
                                    t.setPriority(t.getPriority().next());
                                }
                            }
                            if (t.getAssignedTo() != null) {
                                User assignedUser = system.getUser(t.getAssignedTo());
                                if (assignedUser.getRole() == Role.DEVELOPER) {
                                    Developer dev = (Developer) assignedUser;
                                    if (!dev.canHandle(t)) {
                                        String devName = t.getAssignedTo();
                                        t.setAssignedTo(null);
                                        t.setStatus(Status.OPEN);

                                        Map<String, String> data = new HashMap<>();
                                        data.put("username", devName);
                                        data.put("reason", "seniority_mismatch");
                                        logHistory(t, "DE-ASSIGNED", newDate, data);

                                        Map<String, String> sData = new HashMap<>();
                                        sData.put("oldStatus", "IN_PROGRESS");
                                        sData.put("newStatus", "OPEN");
                                        logHistory(t, "STATUS_CHANGED", newDate, sData);
                                    }
                                }
                            }
                        }
                    }
                    m.setPriorityIncrementsApplied(requiredIncrements);
                }
            }
        }
    }

    /**
     * Handles the start of the testing phase by a manager
     *
     * @param system The bug tracker system
     * @param user   The user making the request (must be a MANAGER)
     */
    private static void handleStartTestingPhase(final BugTrackerSystem system, final User user) {
        if (user.getRole() != Role.MANAGER) {
            throw new RuntimeException("Only managers.");
        }

        for (Milestone m : system.getMilestones().values()) {
            for (int tid : m.getTickets()) {
                Ticket t = system.getTicket(tid);
                if (t != null && t.getStatus() != Status.CLOSED) {
                    throw new RuntimeException("Cannot start: active milestones exist.");
                }
            }
        }

        system.setState(new TestingState());
        system.setPhaseStartDate(system.getCurrentDate());
    }

    /**
     * Finds the milestone for a given ticket ID
     *
     * @param system   The bug tracker system
     * @param ticketId The ID of the ticket
     * @return The milestone containing the ticket, or null if not found
     */
    public static Milestone findMilestoneForTicket(final BugTrackerSystem system,
            final int ticketId) {
        for (Milestone m : system.getMilestones().values()) {
            if (m.getTickets().contains(ticketId)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Checks if a milestone is blocked by another milestone
     *
     * @param system The bug tracker system
     * @param target The milestone to check
     * @return True if the milestone is blocked, false otherwise
     */
    public static boolean isMilestoneBlocked(final BugTrackerSystem system,
            final Milestone target) {
        for (Milestone m : system.getMilestones().values()) {
            if (m.getBlockingFor() != null && m.getBlockingFor().contains(target.getName())) {
                boolean allClosed = true;
                for (int tId : m.getTickets()) {
                    Ticket t = system.getTicket(tId);
                    if (t != null && t.getStatus() != Status.CLOSED) {
                        allClosed = false;
                        break;
                    }
                }
                if (!allClosed) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Views the notifications for a user
     *
     * @param system The bug tracker system
     * @param user   The user to view notifications for
     * @param result The output object node
     */
    private static void handleViewNotifications(final BugTrackerSystem system, final User user,
            final ObjectNode result) {
        ArrayNode arr = result.putArray("notifications");
        List<String> notifs = user.getNotifications();
        for (String n : notifs) {
            arr.add(n);
        }
        user.clearNotifications();
    }

}
