package main.system.state;

// Concrete State for the State Pattern
// When in Development, we can generally do everything: add features, assign tickets, etc
public class DevelopmentState implements WorkflowState {
    @Override
    public boolean canReportTicket() {
        return true;
    }

    @Override
    public boolean canCreateMilestone() {
        return true;
    }

    @Override
    public boolean canAssignTicket() {
        return true;
    }

    @Override
    public boolean canResolveTicket() {
        return true;
    }

    @Override
    public String getName() {
        return "DEVELOPMENT";
    }
}
