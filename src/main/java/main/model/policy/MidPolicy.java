package main.model.policy;

import main.model.enums.Priority;
import main.model.ticket.Ticket;

/**
 * Policy for Mid seniority
 */
public final class MidPolicy implements SeniorityPolicy {
    @Override
    public boolean canHandle(final Ticket ticket) {
        return ticket.getPriority() != Priority.CRITICAL;
    }
}
