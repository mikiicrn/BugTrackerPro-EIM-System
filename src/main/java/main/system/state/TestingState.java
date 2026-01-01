package main.system.state;

// Concrete State for the State Pattern
// When we are testing, we can't add new features (milestones) or assign normal dev work
// We focus on finding bugs
public class TestingState implements WorkflowState {
    @Override
    public boolean canReportTicket() {
        return true;
    }

    @Override
    public boolean canCreateMilestone() {
        return false;
    }

    @Override
    public boolean canAssignTicket() {
        return false;
    }

    @Override
    public boolean canResolveTicket() {
        return false;
    }

    @Override
    public String getName() {
        return "TESTING";
    }
}
