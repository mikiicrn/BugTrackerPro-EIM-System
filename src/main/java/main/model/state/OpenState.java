package main.model.state;

import main.model.enums.Status;

/**
 * State representing a ticket that is Open
 */
public final class OpenState implements TicketState {
    @Override
    public Status nextStatus() {
        return Status.IN_PROGRESS;
    }
}
