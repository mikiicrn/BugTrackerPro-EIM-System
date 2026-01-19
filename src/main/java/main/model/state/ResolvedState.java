package main.model.state;

import main.model.enums.Status;

/**
 * State representing a ticket that is Resolved
 */
public final class ResolvedState implements TicketState {
    @Override
    public Status nextStatus() {
        return Status.CLOSED;
    }
}
