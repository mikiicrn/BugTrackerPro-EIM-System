package main.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a set of tickets to be solved by a set date
 */
public class Milestone {
    private String name;
    private List<String> blockingFor;
    private LocalDate dueDate;
    private List<Integer> tickets;
    private List<String> assignedDevs;
    private String creator;
    private LocalDate creationDate;
    private int priorityIncrementsApplied;
    private boolean notifiedBeforeDeadline;

    /**
     * Constructs a new Milestone
     *
     * @param name         The name of the milestone
     * @param blockingFor  List of milestones this one blocks
     * @param dueDate      The due date
     * @param tickets      List of ticket IDs
     * @param assignedDevs List of assigned developer usernames
     * @param creator      The creator's username
     * @param creationDate The creation date
     */
    public Milestone(final String name, final List<String> blockingFor, final LocalDate dueDate,
            final List<Integer> tickets, final List<String> assignedDevs,
            final String creator, final LocalDate creationDate) {
        this.name = name;
        this.blockingFor = blockingFor == null ? new ArrayList<>() : blockingFor;
        this.dueDate = dueDate;
        this.tickets = tickets == null ? new ArrayList<>() : tickets;
        this.assignedDevs = assignedDevs == null ? new ArrayList<>() : assignedDevs;
        this.creator = creator;
        this.creationDate = creationDate;
        this.priorityIncrementsApplied = 0;
        this.notifiedBeforeDeadline = false;
    }

    /**
     * Gets the milestone name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of milestones this one blocks
     *
     * @return The list of blocked milestone names
     */
    public List<String> getBlockingFor() {
        return blockingFor;
    }

    /**
     * Gets the due date
     *
     * @return The due date
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Gets the list of ticket IDs
     *
     * @return The list of ticket IDs
     */
    public List<Integer> getTickets() {
        return tickets;
    }

    /**
     * Gets the list of assigned developers
     *
     * @return The list of usernames
     */
    public List<String> getAssignedDevs() {
        return assignedDevs;
    }

    /**
     * Gets the creator's username
     *
     * @return The creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Gets the creation date
     *
     * @return The creation date
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     * Gets the number of priority increments applied
     *
     * @return The count
     */
    public int getPriorityIncrementsApplied() {
        return priorityIncrementsApplied;
    }

    /**
     * Sets the number of priority increments applied
     *
     * @param val The new count
     */
    public void setPriorityIncrementsApplied(final int val) {
        this.priorityIncrementsApplied = val;
    }

    /**
     * Checks if notification before deadline has been sent
     *
     * @return True if notified
     */
    public boolean isNotifiedBeforeDeadline() {
        return notifiedBeforeDeadline;
    }

    /**
     * Sets whether notification before deadline has been sent
     *
     * @param val The new value
     */
    public void setNotifiedBeforeDeadline(final boolean val) {
        this.notifiedBeforeDeadline = val;
    }

    /**
     * Adds a ticket ID to the milestone
     *
     * @param ticketId The ticket ID to add
     */
    public void addTicket(final int ticketId) {
        tickets.add(ticketId);
    }
}
