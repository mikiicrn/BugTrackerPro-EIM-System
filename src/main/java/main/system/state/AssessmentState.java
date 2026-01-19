package main.system.state;

/**
 * State representing the assessment phase
 */
public class AssessmentState implements WorkflowState {
    /**
     * Checks if tickets can be reported in this state
     *
     * @return False
     */
    @Override
    public boolean canReportTicket() {
        return false;
    }

    /**
     * Checks if milestones can be created in this state
     *
     * @return False
     */
    @Override
    public boolean canCreateMilestone() {
        return false;
    }

    /**
     * Checks if tickets can be assigned in this state
     *
     * @return False
     */
    @Override
    public boolean canAssignTicket() {
        return false;
    }

    /**
     * Checks if tickets can be resolved in this state
     *
     * @return False
     */
    @Override
    public boolean canResolveTicket() {
        return false;
    }

    /**
     * Gets the name of the state
     *
     * @return "ASSESSMENT"
     */
    @Override
    public String getName() {
        return "ASSESSMENT";
    }
}
