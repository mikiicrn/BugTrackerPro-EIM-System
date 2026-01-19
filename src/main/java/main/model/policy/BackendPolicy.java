package main.model.policy;

import main.model.enums.ExpertiseArea;

/**
 * Policy for Backend expertise
 */
public final class BackendPolicy implements ExpertisePolicy {
    @Override
    public boolean covers(final ExpertiseArea ticketArea) {
        return ticketArea == ExpertiseArea.BACKEND || ticketArea == ExpertiseArea.DB;
    }
}
