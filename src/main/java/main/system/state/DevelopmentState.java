package main.system.state;

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
