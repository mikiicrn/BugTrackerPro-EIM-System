package main.system.state;

/**
 * Interface defining the methods for workflow states
 */
public interface WorkflowState {
    /**
     * Checks if tickets can be reported
     *
     * @return True if tickets can be reported, false otherwise
     */
    boolean canReportTicket();

    /**
     * Checks if milestones can be created
     *
     * @return True if milestones can be created, false otherwise
     */
    boolean canCreateMilestone();

    /**
     * Checks if tickets can be assigned
     *
     * @return True if tickets can be assigned, false otherwise
     */
    boolean canAssignTicket();

    /**
     * Checks if tickets can be resolved
     *
     * @return True if tickets can be resolved, false otherwise
     */
    boolean canResolveTicket();

    /**
     * Gets the name of the state
     *
     * @return The state name
     */
    String getName();
}
