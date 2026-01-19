package main.system.state;

/**
 * State representing the testing phase
 */
public class TestingState implements WorkflowState {
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
     * @return "TESTING"
     */
    @Override
    public String getName() {
        return "TESTING";
    }
}
