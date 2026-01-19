package main.model.state;

import main.model.enums.Status;

/**
 * Interface representing the state of a ticket
 */
public interface TicketState {
    /**
     * Determines the next status in the workflow
     *
     * @return The next status, or null if no transition is allowed
     */
    Status nextStatus();
}
