package main.model.policy;

import main.model.enums.ExpertiseArea;

/**
 * Policy for Frontend expertise
 */
public final class FrontendPolicy implements ExpertisePolicy {
    @Override
    public boolean covers(final ExpertiseArea ticketArea) {
        return ticketArea == ExpertiseArea.FRONTEND || ticketArea == ExpertiseArea.DESIGN;
    }
}
