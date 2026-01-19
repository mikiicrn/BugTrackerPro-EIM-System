package main.model.state;

import main.model.enums.Status;

/**
 * State representing a ticket that is Closed
 */
public final class ClosedState implements TicketState {
    @Override
    public Status nextStatus() {
        // No transition from CLOSED
        return null;
    }
}
