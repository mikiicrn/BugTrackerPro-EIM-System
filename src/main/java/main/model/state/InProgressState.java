package main.model.state;

import main.model.enums.Status;

/**
 * State representing a ticket that is In Progress
 */
public final class InProgressState implements TicketState {
    @Override
    public Status nextStatus() {
        return Status.RESOLVED;
    }
}
