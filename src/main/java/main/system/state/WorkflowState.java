package main.system.state;

// State Pattern Interface
// This defines what operations are allowed in the different states of the project
// Concrete states (Testing, Development) will implement these
public interface WorkflowState {
    boolean canReportTicket();

    boolean canCreateMilestone();

    boolean canAssignTicket();

    boolean canResolveTicket();

    String getName();
}
