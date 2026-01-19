package main.model.policy;

import main.model.enums.ExpertiseArea;

/**
 * Policy for Fullstack expertise
 */
public final class FullstackPolicy implements ExpertisePolicy {
    @Override
    public boolean covers(final ExpertiseArea ticketArea) {
        return true;
    }
}
