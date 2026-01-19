package main.model.policy;

import main.model.enums.ExpertiseArea;

/**
 * Interface for checking if a developer's expertise matches the ticket's
 * requirements
 */
public interface ExpertisePolicy {
    /**
     * Checks if the ticket area is covered by this policy
     *
     * @param ticketArea The expertise area required by the ticket
     * @return True if covered, false otherwise
     */
    boolean covers(ExpertiseArea ticketArea);
}
