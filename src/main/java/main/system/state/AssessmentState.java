package main.system.state;

public class AssessmentState implements WorkflowState {
    @Override
    public boolean canReportTicket() {
        return false;
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
        return "ASSESSMENT";
    }
}
