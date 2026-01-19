package main.system;

import main.model.Milestone;
import main.model.ticket.Ticket;
import main.model.user.User;
import main.model.enums.Priority;
import main.model.enums.Status;
import main.system.state.WorkflowState;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.command.CommandRunner;
import main.model.user.Developer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Central System Registry (Singleton)
 *
 * This class acts as the "Single Source of Truth" for the entire application
 * state
 * It manages the lifecycle and storage of all core entities: Users, Tickets,
 * and Milestones
 *
 * Design Patterns Implemented:
 * - Singleton: Ensures exactly one instance of the system exists to maintain
 * global consistency
 * - State: Delegates workflow-specific behavior to the current WorkflowState
 * - Facade: Provides a simplified interface for commands to interact with the
 * complex web of domain objects
 */
public final class BugTrackerSystem {

    private static BugTrackerSystem instance;

    // Core Data Structures - using Maps for O(1) retrieval by key (username, name)
    private Map<String, User> users;
    private List<Ticket> tickets; // List used here as sequential ID access is common, though Map could optimized
                                  // lookup
    private int ticketCounter;
    private Map<String, Milestone> milestones;

    // Behavioral Components
    private WorkflowState currentState;
    private NotificationService notificationService;

    // Temporal State
    private LocalDate currentDate;
    private LocalDate startDate;
    private List<ObjectNode> outputs;
    private LocalDate phaseStartDate;
    private boolean active = true;

    private BugTrackerSystem() {
        users = new HashMap<>();
        tickets = new ArrayList<>();
        ticketCounter = 0;
        milestones = new HashMap<>();
        notificationService = new NotificationService(this);
    }

    /**
     * Proactive Deadline Monitoring
     *
     * Checks if a milestone is due "tomorrow" relative to the current simulation
     * date
     * If so, it triggers an alert and escalates the priority of all unresolved
     * tickets to CRITICAL
     *
     * Side Effects:
     * - Sends notifications to assigned developers
     * - Modifies ticket state (priority escalation)
     * - Potentially triggers de-assignment if the assignee is not qualified for
     * critical tasks
     */
    private void checkDeadlineForMilestone(final Milestone m, final LocalDate date) {
        if (!m.isNotifiedBeforeDeadline()) {
            notificationService.notifyUsers(m.getAssignedDevs(), "Milestone " + m.getName()
                    + " is due tomorrow. All unresolved tickets are now CRITICAL.");
            m.setNotifiedBeforeDeadline(true);

            // Iterate and escalate
            for (Integer tid : m.getTickets()) {
                Ticket t = getTicket(tid);
                if (t != null && t.getStatus() != Status.CLOSED
                        && t.getStatus() != Status.RESOLVED) {
                    processCriticalTicket(t, date);
                }
            }
        }
    }

    /**
     * Handles the automated escalation of a ticket to CRITICAL priority
     *
     * Role-Based Validation:
     * Critical tickets require higher seniority handling. This method verifies if
     * the currently
     * assigned developer is capable of handling the escalated priority. If not
     * (e.g., a Junior
     * assigned to a ticket that becomes Critical), the system automatically
     * un-assigns them
     * to prevent process violation
     *
     * @param t    The ticket being escalated
     * @param date The current date of the event
     */
    private void processCriticalTicket(final Ticket t, final LocalDate date) {
        t.setPriority(Priority.CRITICAL);
        if (t.getAssignedTo() != null) {
            User assignedUser = getUser(t.getAssignedTo());
            if (assignedUser.getRole() == main.model.enums.Role.DEVELOPER) {
                Developer dev = (Developer) assignedUser;
                // Seniority Check: Can this dev handle the heat?
                if (!dev.canHandle(t)) {
                    String devName = t.getAssignedTo();
                    t.setAssignedTo(null);
                    t.setStatus(Status.OPEN); // Revert to OPEN for re-assignment

                    // Audit Log: Record the forced de-assignment
                    Map<String, String> data = new HashMap<>();
                    data.put("username", devName);
                    data.put("reason", "seniority_mismatch");
                    CommandRunner.logHistory(t, "DE-ASSIGNED", date, data);

                    Map<String, String> sData = new HashMap<>();
                    sData.put("oldStatus", "IN_PROGRESS");
                    sData.put("newStatus", "OPEN");
                    CommandRunner.logHistory(t, "STATUS_CHANGED", date, sData);
                }
            }
        }
    }

    /**
     * Thread-safe retrieval of the Singleton instance
     *
     * Uses synchronized access to preventing race conditions during initialization,
     * though in this single-threaded simulation it's primarily for architectural
     * correctness
     *
     * @return The global system instance
     */
    public static synchronized BugTrackerSystem getInstance() {
        if (instance == null) {
            instance = new BugTrackerSystem();
        }
        return instance;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void addUser(final User user) {
        users.put(user.getUsername(), user);
    }

    public User getUser(final String username) {
        return users.get(username);
    }

    public void addTicket(final Ticket ticket) {
        tickets.add(ticket);
    }

    /**
     * Retrieves a ticket by its numerical ID
     *
     * @param id The unique ticket ID
     * @return The Ticket object, or null if not found
     */
    public Ticket getTicket(final int id) {
        // Linear search (O(N)). Acceptable for current scale, but candidate for
        // refactoring to HashMap<Integer, Ticket>
        for (Ticket t : tickets) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public List<Ticket> getAllTickets() {
        return tickets;
    }

    public int getNextTicketId() {
        return ticketCounter++;
    }

    public void addMilestone(final Milestone milestone) {
        milestones.put(milestone.getName(), milestone);
    }

    public Milestone getMilestone(final String name) {
        return milestones.get(name);
    }

    public Map<String, Milestone> getMilestones() {
        return milestones;
    }

    public void setState(final WorkflowState state) {
        this.currentState = state;
    }

    public WorkflowState getState() {
        return currentState;
    }

    public void setOutputs(final List<ObjectNode> outputs) {
        this.outputs = outputs;
    }

    public void addOutput(final ObjectNode output) {
        if (outputs != null) {
            outputs.add(output);
        }
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    /**
     * Advances the system clock and triggers time-sensitive logic
     *
     * This makes the system "tick". Every time the date is updated via a command,
     * the system checks if any milestones have reached their deadline threshold
     *
     * @param currentDate The new date of the simulation
     */
    public void setCurrentDate(final LocalDate currentDate) {
        if (this.startDate == null) {
            this.startDate = currentDate;
        }
        checkDeadlines(currentDate);
        this.currentDate = currentDate;
    }

    private void checkDeadlines(final LocalDate date) {
        for (Milestone m : milestones.values()) {
            // Check if deadline is exactly tomorrow
            if (m.getDueDate().equals(date.plusDays(1))) {
                checkDeadlineForMilestone(m, date);
            }
        }
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getPhaseStartDate() {
        return phaseStartDate;
    }

    public void setPhaseStartDate(final LocalDate phaseStartDate) {
        this.phaseStartDate = phaseStartDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Resets the system to a clean state
     *
     * Crucial for isolation between test cases. Clears all registries and resets
     * counters
     * to ensure no state leakage persists between runs
     */
    public void reset() {
        users.clear();
        tickets.clear();
        ticketCounter = 0;
        milestones.clear();
        currentState = null;
        currentDate = null;
        startDate = null;
        phaseStartDate = null;
        outputs = null;
        active = true;
    }
}
