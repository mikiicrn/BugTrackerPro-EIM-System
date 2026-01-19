package main.model.policy;

import main.model.enums.ExpertiseArea;

/**
 * Policy for DB expertise
 */
public final class DbPolicy implements ExpertisePolicy {
    @Override
    public boolean covers(final ExpertiseArea ticketArea) {
        return ticketArea == ExpertiseArea.DB;
    }
}
