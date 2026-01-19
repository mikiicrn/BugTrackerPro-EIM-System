package main.model.policy;

import main.model.enums.Priority;
import main.model.enums.TicketType;
import main.model.ticket.Ticket;

/**
 * Policy for Junior seniority
 */
public final class JuniorPolicy implements SeniorityPolicy {
    @Override
    public boolean canHandle(final Ticket ticket) {
        return (ticket.getPriority() == Priority.LOW || ticket.getPriority() == Priority.MEDIUM)
                && (ticket.getType() == TicketType.BUG
                        || ticket.getType() == TicketType.UI_FEEDBACK);
    }
}
