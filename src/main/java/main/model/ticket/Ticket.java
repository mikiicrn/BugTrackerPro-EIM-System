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

public abstract class Ticket {
    // Represents a task or issue in the system
    // Could be a Bug, a Feature Request, or just Feedback
    // It has a lifecycle (OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED)
    private int id;
    private TicketType type;
    private String title;
    private Priority priority;
    private Status status;
    private ExpertiseArea expertiseArea;
    private String description;
    private String reportedBy;
    private String assignedTo;

    // New fields
    private LocalDate createdAt;
    private LocalDate solvedAt;
    private LocalDate assignedAt;
    private List<Map<String, String>> comments = new ArrayList<>();

    // Status Change Inner Class
    public static class StatusChange {
        private Status oldStatus;
        private Status newStatus;

        public StatusChange(Status old, Status newStat) {
            this.oldStatus = old;
            this.newStatus = newStat;
        }

        public Status getOldStatus() {
            return oldStatus;
        }

        public Status getNewStatus() {
            return newStatus;
        }
    }

    private LinkedList<StatusChange> statusHistory = new LinkedList<>();

    public Ticket(int id, TicketType type, String title, Priority priority, Status status,
            ExpertiseArea expertiseArea, String description, String reportedBy) {
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

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(LocalDate solvedAt) {
        this.solvedAt = solvedAt;
    }

    public LocalDate getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDate assignedAt) {
        this.assignedAt = assignedAt;
    }

    public List<Map<String, String>> getComments() {
        return comments;
    }

    public void addComment(Map<String, String> comment) {
        this.comments.add(comment);
    }

    public void removeLastComment() {
        if (!comments.isEmpty())
            comments.remove(comments.size() - 1);
    }

    public int getId() {
        return id;
    }

    public TicketType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (this.status != null && this.status != status) {
            statusHistory.push(new StatusChange(this.status, status));
        }
        this.status = status;
    }

    public void revertStatus() {
        if (!statusHistory.isEmpty()) {
            StatusChange change = statusHistory.pop();
            this.status = change.getOldStatus();
        }
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public String getDescription() {
        return description;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    // History Tracking
    public static class HistoryEntry {
        private String type; // ASSIGNED, DE-ASSIGNED, STATUS_CHANGED, ADDED_TO_MILESTONE, REMOVED_FROM_DEV
        private String details;
        private LocalDate date;
        // Depending on requirements, might need more fields, but for now simple string
        // rep or structured map.
        // The output requirement for history seems detailed.
        // Example output shows: "type": "ASSIGNED", "date": "...", "username": "..."
        // etc.
        // So we should store structured data.
        private Map<String, String> data;

        public HistoryEntry(String type, LocalDate date, Map<String, String> data) {
            this.type = type;
            this.date = date;
            this.data = data;
        }

        public String getType() {
            return type;
        }

        public LocalDate getDate() {
            return date;
        }

        public Map<String, String> getData() {
            return data;
        }
    }

    private List<HistoryEntry> history = new ArrayList<>();

    public void addHistory(String type, LocalDate date, Map<String, String> data) {
        history.add(new HistoryEntry(type, date, data));
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }

    public List<StatusChange> getStatusHistory() {
        return statusHistory;
    }
}
