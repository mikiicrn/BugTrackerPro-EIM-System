package main.model.policy;

import main.model.enums.ExpertiseArea;

/**
 * Policy for DevOps expertise
 */
public final class DevOpsPolicy implements ExpertisePolicy {
    @Override
    public boolean covers(final ExpertiseArea ticketArea) {
        return ticketArea == ExpertiseArea.DEVOPS;
    }
}
