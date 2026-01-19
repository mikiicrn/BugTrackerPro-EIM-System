package main.model.policy;

import main.model.ticket.Ticket;

/**
 * Interface for verifying if a developer of a certain seniority can handle a
 * ticket
 */
public interface SeniorityPolicy {
    /**
     * Checks if the ticket meets the criteria for this seniority level
     *
     * @param ticket The ticket to check
     * @return True if allowed, false otherwise
     */
    boolean canHandle(Ticket ticket);
}
