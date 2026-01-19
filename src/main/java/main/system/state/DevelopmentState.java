package main.system.state;

/**
 * State representing the development phase
 */
public class DevelopmentState implements WorkflowState {
    /**
     * Checks if tickets can be reported in this state
     *
     * @return True
     */
    @Override
    public boolean canReportTicket() {
        return true;
    }

    /**
     * Checks if milestones can be created in this state
     *
     * @return True
     */
    @Override
    public boolean canCreateMilestone() {
        return true;
    }

    /**
     * Checks if tickets can be assigned in this state
     *
     * @return True
     */
    @Override
    public boolean canAssignTicket() {
        return true;
    }

    /**
     * Checks if tickets can be resolved in this state
     *
     * @return True
     */
    @Override
    public boolean canResolveTicket() {
        return true;
    }

    /**
     * Gets the name of the state
     *
     * @return "DEVELOPMENT"
     */
    @Override
    public String getName() {
        return "DEVELOPMENT";
    }
}
