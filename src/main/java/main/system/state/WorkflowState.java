package main.system.state;

public interface WorkflowState {
    boolean canReportTicket();

    boolean canCreateMilestone();

    boolean canAssignTicket();

    boolean canResolveTicket();

    String getName();
}
