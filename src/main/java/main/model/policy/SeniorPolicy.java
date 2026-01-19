package main.model.policy;

import main.model.ticket.Ticket;

/**
 * Policy for Senior seniority
 */
public final class SeniorPolicy implements SeniorityPolicy {
    @Override
    public boolean canHandle(final Ticket ticket) {
        return true;
    }
}
