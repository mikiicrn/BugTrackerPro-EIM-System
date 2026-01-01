package main.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Milestone {
    // A collection of tickets that need to be done by a specific deadline
    // Like a "Sprint" or a "Release"
    private String name;
    private List<String> blockingFor; // Names of milestones this one blocks
    private LocalDate dueDate;
    private List<Integer> tickets;
    private List<String> assignedDevs;
    private String creator; // Manager username
    private LocalDate creationDate;
    private int priorityIncrementsApplied;
    private boolean notifiedBeforeDeadline;

    public Milestone(String name, List<String> blockingFor, LocalDate dueDate, List<Integer> tickets,
            List<String> assignedDevs, String creator, LocalDate creationDate) {
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

    public String getName() {
        return name;
    }

    public List<String> getBlockingFor() {
        return blockingFor;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public List<Integer> getTickets() {
        return tickets;
    }

    public List<String> getAssignedDevs() {
        return assignedDevs;
    }

    public String getCreator() {
        return creator;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public int getPriorityIncrementsApplied() {
        return priorityIncrementsApplied;
    }

    public void setPriorityIncrementsApplied(int val) {
        this.priorityIncrementsApplied = val;
    }

    public boolean isNotifiedBeforeDeadline() {
        return notifiedBeforeDeadline;
    }

    public void setNotifiedBeforeDeadline(boolean val) {
        this.notifiedBeforeDeadline = val;
    }

    public void addTicket(int ticketId) {
        tickets.add(ticketId);
    }
}
