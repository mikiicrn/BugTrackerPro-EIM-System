package main.model.policy;

import main.model.enums.ExpertiseArea;

/**
 * Policy for Design expertise
 */
public final class DesignPolicy implements ExpertisePolicy {
    @Override
    public boolean covers(final ExpertiseArea ticketArea) {
        return ticketArea == ExpertiseArea.DESIGN || ticketArea == ExpertiseArea.FRONTEND;
    }
}
