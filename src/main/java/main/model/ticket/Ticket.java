package main.model.ticket;

import main.model.enums.ExpertiseArea;
import main.model.enums.Priority;
import main.model.enums.Status;
import main.model.enums.TicketType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Abstract class representing a ticket in the bug tracker system
 */
public abstract class Ticket {

    private int id;
    private TicketType type;
    private String title;
    private Priority priority;
    private Status status;
    private ExpertiseArea expertiseArea;
    private String description;
    private String reportedBy;
    private String assignedTo;

    private LocalDate createdAt;
    private LocalDate solvedAt;
    private LocalDate assignedAt;
    private List<Map<String, String>> comments = new ArrayList<>();

    /**
     * Represents a change in ticket status
     */
    public static class StatusChange {
        private Status oldStatus;
        private Status newStatus;

        /**
         * Constructs a new StatusChange
         *
         * @param old     The old status
         * @param newStat The new status
         */
        public StatusChange(final Status old, final Status newStat) {
            this.oldStatus = old;
            this.newStatus = newStat;
        }

        /**
         * Gets the old status
         *
         * @return The old status
         */
        public Status getOldStatus() {
            return oldStatus;
        }

        /**
         * Gets the new status
         *
         * @return The new status
         */
        public Status getNewStatus() {
            return newStatus;
        }
    }

    private LinkedList<StatusChange> statusHistory = new LinkedList<>();

    /**
     * Constructs a new Ticket
     *
     * @param id            The ticket ID
     * @param type          The ticket type
     * @param title         The ticket title
     * @param priority      The ticket priority
     * @param status        The initial status
     * @param expertiseArea The required expertise area
     * @param description   The description
     * @param reportedBy    The username of the reporter
     */
    public Ticket(final int id, final TicketType type, final String title,
            final Priority priority, final Status status,
            final ExpertiseArea expertiseArea, final String description,
            final String reportedBy) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.expertiseArea = expertiseArea;
        this.description = description;
        this.reportedBy = reportedBy;
        this.assignedTo = null;
    }

    /**
     * Gets the creation date
     *
     * @return The creation date
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation date
     *
     * @param createdAt The creation date
     */
    public void setCreatedAt(final LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the solved date
     *
     * @return The solved date
     */
    public LocalDate getSolvedAt() {
        return solvedAt;
    }

    /**
     * Sets the solved date
     *
     * @param solvedAt The solved date
     */
    public void setSolvedAt(final LocalDate solvedAt) {
        this.solvedAt = solvedAt;
    }

    /**
     * Gets the assigned date
     *
     * @return The assigned date
     */
    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    /**
     * Sets the assigned date
     *
     * @param assignedAt The assigned date
     */
    public void setAssignedAt(final LocalDate assignedAt) {
        this.assignedAt = assignedAt;
    }

    /**
     * Gets the list of comments
     *
     * @return The list of comments
     */
    public List<Map<String, String>> getComments() {
        return comments;
    }

    /**
     * Adds a comment to the ticket
     *
     * @param comment The comment map
     */
    public void addComment(final Map<String, String> comment) {
        this.comments.add(comment);
    }

    /**
     * Removes the last added comment
     */
    public void removeLastComment() {
        if (!comments.isEmpty()) {
            comments.remove(comments.size() - 1);
        }
    }

    /**
     * Gets the ticket ID
     *
     * @return The ticket ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the ticket type
     *
     * @return The ticket type
     */
    public TicketType getType() {
        return type;
    }

    /**
     * Gets the ticket title
     *
     * @return The ticket title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the ticket priority
     *
     * @return The ticket priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Sets the ticket priority
     *
     * @param priority The new priority
     */
    public void setPriority(final Priority priority) {
        this.priority = priority;
    }

    /**
     * Gets the ticket status
     *
     * @return The ticket status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the ticket status and records the history
     *
     * @param status The new status
     */
    public void setStatus(final Status status) {
        if (this.status != null && this.status != status) {
            statusHistory.push(new StatusChange(this.status, status));
        }
        this.status = status;
    }

    /**
     * Reverts the status to the previous one
     */
    public void revertStatus() {
        if (!statusHistory.isEmpty()) {
            StatusChange change = statusHistory.pop();
            this.status = change.getOldStatus();
        }
    }

    /**
     * Gets the required expertise area
     *
     * @return The expertise area
     */
    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the reporter's username
     *
     * @return The reporter's username
     */
    public String getReportedBy() {
        return reportedBy;
    }

    /**
     * Gets the assigned developer's username
     *
     * @return The assigned user
     */
    public String getAssignedTo() {
        return assignedTo;
    }

    /**
     * Sets the assigned developer's username
     *
     * @param assignedTo The username
     */
    public void setAssignedTo(final String assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * Represents an entry in the ticket's history
     */
    public static class HistoryEntry {
        private String type;
        private LocalDate date;
        private Map<String, String> data;

        /**
         * Constructs a new HistoryEntry
         *
         * @param type The type of entry
         * @param date The date of the entry
         * @param data The data associated with the entry
         */
        public HistoryEntry(final String type, final LocalDate date,
                final Map<String, String> data) {
            this.type = type;
            this.date = date;
            this.data = data;
        }

        /**
         * Gets the type of the history entry
         *
         * @return The type
         */
        public String getType() {
            return type;
        }

        /**
         * Gets the date of the history entry
         *
         * @return The date
         */
        public LocalDate getDate() {
            return date;
        }

        /**
         * Gets the data map for the history entry
         *
         * @return The data map
         */
        public Map<String, String> getData() {
            return data;
        }
    }

    private List<HistoryEntry> history = new ArrayList<>();

    /**
     * Adds a history entry to the ticket
     *
     * @param entryType The type of history entry
     * @param date      The date of the entry
     * @param data      The data associated with the entry
     */
    public void addHistory(final String entryType, final LocalDate date,
            final Map<String, String> data) {
        history.add(new HistoryEntry(entryType, date, data));
    }

    /**
     * Gets the list of history entries
     *
     * @return The history list
     */
    public List<HistoryEntry> getHistory() {
        return history;
    }

    /**
     * Gets the list of status changes
     *
     * @return The list of status changes
     */
    public List<StatusChange> getStatusHistory() {
        return statusHistory;
    }

    /**
     * Calculates the risk score for this ticket
     *
     * @return The risk score
     */
    public abstract double calculateRisk();

    /**
     * Calculates the resolution efficiency score for this ticket
     *
     * @param daysToResolve The number of days taken to resolve the ticket
     * @return The efficiency score
     */
    public abstract double calculateEfficiency(long daysToResolve);

    /**
     * Calculates the raw impact score for this ticket
     *
     * @param daysOpen The number of days the ticket has been open
     * @return The raw impact score
     */
    public abstract double calculateRawImpact(long daysOpen);

    /**
     * Gets the maximum value for impact normalization
     *
     * @return The maximum impact value
     */
    public abstract double getImpactMax();

    /**
     * Gets the maximum value for stability normalization
     *
     * @return The maximum stability value
     */
    public abstract double getStabilityMax();
}
