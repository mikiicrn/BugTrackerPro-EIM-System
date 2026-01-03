package main.system;

import main.model.Milestone;
import main.model.ticket.Ticket;
import main.model.user.User;
import main.system.state.WorkflowState;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// singleton class for the system, i store all the data here users tickets and milestones
public class BugTrackerSystem {

    private static BugTrackerSystem instance;

    private Map<String, User> users;
    private List<Ticket> tickets;
    private int ticketCounter;
    private Map<String, Milestone> milestones;
    private WorkflowState currentState;

    // private constructor so i ensure only one instance exists
    private BugTrackerSystem() {
        users = new HashMap<>();
        tickets = new ArrayList<>();
        ticketCounter = 0;
        milestones = new HashMap<>();
    }

    // get the single instance
    public static synchronized BugTrackerSystem getInstance() {
        if (instance == null) {
            instance = new BugTrackerSystem();
        }
        return instance;
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public Ticket getTicket(int id) {
        for (Ticket t : tickets) {
            if (t.getId() == id)
                return t;
        }
        return null;
    }

    public List<Ticket> getAllTickets() {
        return tickets;
    }

    public int getNextTicketId() {
        return ticketCounter++;
    }

    public void addMilestone(Milestone milestone) {
        milestones.put(milestone.getName(), milestone);
    }

    public Milestone getMilestone(String name) {
        return milestones.get(name);
    }

    public Map<String, Milestone> getMilestones() {
        return milestones;
    }

    public void setState(WorkflowState state) {
        this.currentState = state;
    }

    public WorkflowState getState() {
        return currentState;
    }

    private LocalDate currentDate;
    private LocalDate startDate;
    private List<ObjectNode> outputs;

    public void setOutputs(List<ObjectNode> outputs) {
        this.outputs = outputs;
    }

    public void addOutput(ObjectNode output) {
        if (outputs != null) {
            outputs.add(output);
        }
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        if (this.startDate == null) {
            this.startDate = currentDate;
        }
        this.currentDate = currentDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    private LocalDate phaseStartDate;

    public LocalDate getPhaseStartDate() {
        return phaseStartDate;
    }

    public void setPhaseStartDate(LocalDate phaseStartDate) {
        this.phaseStartDate = phaseStartDate;
    }

    private boolean active = true;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // clean slate for the system, useful between tests
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
